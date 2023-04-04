package com.facturation.service.impl;

import com.facturation.dto.FactureDto;
import com.facturation.dto.LigneFactureDto;
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
import com.facturation.validator.FactureValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FactureServiceImpl implements FactureService {

    private FactureRepository factureRepository;
    private ClientRepository clientRepository;
    private ProduitRepository produitRepository;
    private LigneFactureRepository ligneFactureRepository;

    @Autowired
    public FactureServiceImpl(FactureRepository factureRepository,ClientRepository clientRepository,ProduitRepository produitRepository,LigneFactureRepository ligneFactureRepository) {
        this.factureRepository = factureRepository;
        this.clientRepository = clientRepository;
        this.produitRepository = produitRepository;
        this.ligneFactureRepository = ligneFactureRepository;
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
                        log.error("prodtui not fond dans facture ",ligneFactureDto.getProduit().getId());
                        produitErrors.add("prodtui introvable '");
                    }
                }
            }
        }

        if(!produitErrors.isEmpty()) {
            log.warn("");
            throw new InvalidEntityException("prodiot n'existe pas dans la BDD",ErrorCodes.PRODUIT_NOT_FOUND,produitErrors);
        }

        int tauxTva = 7;
        dto.setTauxTVA(tauxTva);
        Facture saveFacture = factureRepository.save(FactureDto.toEntity(dto));

        double montantTotalProduit = 0.0;
        if(dto.getLignesFacture() != null) {
            for (LigneFactureDto ligneFact : dto.getLignesFacture()) {
                montantTotalProduit = montantTotalProduit + (ligneFact.getProduit().getPrix() * ligneFact.getQuantite());
                LigneFacture ligneFacture=LigneFactureDto.toEntity(ligneFact);
                ligneFacture.setFacture(saveFacture);
                ligneFacture.setPrixUnitaire(ligneFact.getProduit().getPrix());
                ligneFactureRepository.save(ligneFacture);
            }
        }
        double montantTotal = montantTotalProduit + ((montantTotalProduit * tauxTva ) / 100);
        factureRepository.updateMontantTotal(saveFacture.getId(), montantTotalProduit,montantTotal);
        return FactureDto.fromEntity(saveFacture);
    }
}
