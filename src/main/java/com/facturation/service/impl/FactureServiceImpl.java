package com.facturation.service.impl;

import com.facturation.dto.FactureDto;
import com.facturation.dto.LigneFactureDto;
import com.facturation.dto.ProduitDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.Client;
import com.facturation.model.Facture;
import com.facturation.model.LigneFacture;
import com.facturation.model.Produit;
import com.facturation.repository.ClientRepository;
import com.facturation.repository.FactureRepository;
import com.facturation.repository.LigneFactureRepository;
import com.facturation.repository.ProduitRepository;
import com.facturation.service.FactureService;
import com.facturation.service.TimbreFiscalService;
import com.facturation.validator.FactureValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
public class FactureServiceImpl implements FactureService {

    private FactureRepository factureRepository;
    private ClientRepository clientRepository;
    private ProduitRepository produitRepository;
    private LigneFactureRepository ligneFactureRepository;
    private TimbreFiscalService timbreFiscalService;

    @Autowired
    public FactureServiceImpl(FactureRepository factureRepository, ClientRepository clientRepository, ProduitRepository produitRepository, LigneFactureRepository ligneFactureRepository, TimbreFiscalService timbreFiscalService) {
        this.factureRepository = factureRepository;
        this.clientRepository = clientRepository;
        this.produitRepository = produitRepository;
        this.ligneFactureRepository = ligneFactureRepository;
        this.timbreFiscalService = timbreFiscalService;
    }

    @Override
    public FactureDto save(FactureDto dto) {
        List<String> errors = FactureValidator.validate(dto);

        if(!errors.isEmpty()) {
            log.error("Facture is not valid {} ",dto);
            throw new InvalidEntityException("Facture n est pas valide", ErrorCodes.FACTURE_NOT_VALID,errors);
        }

        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
        if(client.isEmpty()){
            log.error("Client not fond dans facture ",dto.getClient().getId());
            throw new EntityNotFoundException("Aucune client trouvée");
        }

        List<String> produitErrors= new ArrayList<String>();
        if(dto.getLignesFacture() != null){
            for (LigneFactureDto ligneFactureDto : dto.getLignesFacture()) {
                if(ligneFactureDto.getProduit() != null){
                    Optional<Produit> produit = produitRepository.findById(ligneFactureDto.getProduit().getId());
                    if(produit.isEmpty()) {
                        log.error("Produit not fond dans facture ",ligneFactureDto.getProduit().getId());
                        produitErrors.add("prodtui introvable '");
                    }
                }
            }
        }

        if(!produitErrors.isEmpty()) {
            log.warn("");
            throw new InvalidEntityException("prodiot n'existe pas dans la BDD",ErrorCodes.PRODUIT_NOT_FOUND,produitErrors);
        }

        int tauxTva = 19;
        dto.setTauxTVA(tauxTva);
        dto.setReference(generateReference(dto.getClient().getCode()));
        dto.setTimbreFiscale(timbreFiscalService.getTimbreFiscale().getMontant());
        Facture saveFacture = factureRepository.save(FactureDto.toEntity(dto));

        double montantTotalProduit = 0.0;
        int remise = dto.getClient().getRemise();
        if(dto.getLignesFacture() != null) {
            for (LigneFactureDto ligneFact : dto.getLignesFacture()) {
                double montantProduit = ligneFact.getProduit().getPrix() * ligneFact.getQuantite();
                if(ligneFact.getProduit().getEtatRemise() == true){
                    montantTotalProduit = montantTotalProduit + (montantProduit - (montantProduit * (remise / 100.0)));
                }else{
                    montantTotalProduit = montantTotalProduit + montantProduit;
                }
                LigneFacture ligneFacture=LigneFactureDto.toEntity(ligneFact);
                ligneFacture.setFacture(saveFacture);
                ligneFacture.setPrixUnitaire(ligneFact.getProduit().getPrix());
                ligneFacture.setRemise(remise);
                ligneFacture.setPrixTotal((montantProduit - (montantProduit * (remise / 100.0))));
                ligneFactureRepository.save(ligneFacture);
            }
        }
        double montantTotal = montantTotalProduit + (montantTotalProduit * (tauxTva / 100.0));
        factureRepository.updateMontantTotal(saveFacture.getId(), montantTotalProduit,montantTotal);
        return FactureDto.fromEntity(saveFacture);
    }
    @Override
    public String generateReference(String codeClient){
        LocalDate today = LocalDate.now();
        int year = today.getYear();

        int numeroFactureParClient = factureRepository.countByYers(year)+1;
        String nombreDeFacturesFormatte = String.format("%04d", numeroFactureParClient);

        return year + "-" + codeClient + "-" +nombreDeFacturesFormatte;
    }

    @Override
    public Page<FactureDto> findAll(Pageable pageable) {
        Page<Facture> factures = factureRepository.findAll(pageable);
        Function<Facture, FactureDto> converter = FactureDto::fromEntity;
        Page<FactureDto> factureDtosPage = factures.map(converter);
        return factureDtosPage;
    }

    @Override
    public FactureDto findById(Long id) {
        if ( id == null) {
            return null;
        }

        Optional<Facture> facture = factureRepository.findById(id);
        FactureDto dto = facture.map(FactureDto::fromEntity).orElse(null);

        if (dto == null) {
            throw new EntityNotFoundException("Aucune facture trouvée dans la base de données",
                    ErrorCodes.FACTURE_NOT_FOUND);
        }

        return dto;
    }
}
