package com.facturation.service.impl;

import com.facturation.dto.DevisDto;
import com.facturation.dto.FactureDto;
import com.facturation.dto.LigneFactureDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.*;
import com.facturation.repository.*;
import com.facturation.service.DevisService;
import com.facturation.service.TimbreFiscalService;
import com.facturation.validator.DevisValidator;
import com.facturation.validator.FactureValidator;
import com.itextpdf.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DevisServiceImpl implements DevisService {

  private DevisRepository devisRepository;
  private ClientRepository clientRepository;
  private ProduitRepository produitRepository;
  private LigneDevisRepository ligneDevisRepository;
  private TimbreFiscalService timbreFiscalService;
  private NumFactureRepository numFactureRepository;
  private TvaRepository tvaRepository;

  @Autowired
  public DevisServiceImpl(
      DevisRepository devisRepository,
      ClientRepository clientRepository,
      ProduitRepository produitRepository,
      LigneDevisRepository ligneDevisRepository,
      TimbreFiscalService timbreFiscalService,
      NumFactureRepository numFactureRepository,
      TvaRepository tvaRepository) {
    this.devisRepository = devisRepository;
    this.clientRepository = clientRepository;
    this.produitRepository = produitRepository;
    this.ligneDevisRepository = ligneDevisRepository;
    this.timbreFiscalService = timbreFiscalService;
    this.numFactureRepository = numFactureRepository;
    this.tvaRepository = tvaRepository;
  }

  @Override
  public Devis save(Devis devis) {

    List<String> errors = DevisValidator.validate(devis);

    if (!errors.isEmpty()) {
      log.error("Facture is not valid {} ", devis);
      throw new InvalidEntityException(
          "Facture n est pas valide", ErrorCodes.FACTURE_NOT_VALID, errors);
    }

    Optional<Client> client = clientRepository.findById(devis.getClient().getId());
    if (client.isEmpty()) {
      log.error("Client not fond dans devis ", devis.getClient().getId());
      throw new EntityNotFoundException("Aucune client trouvée");
    }

    List<String> produitErrors = new ArrayList<String>();
    if (devis.getLigneDevis() != null) {
      for (LigneDevis ligneDevis : devis.getLigneDevis()) {
        if (ligneDevis.getProduit() != null) {
          Optional<Produit> produit = produitRepository.findById(ligneDevis.getProduit().getId());
          if (produit.isEmpty()) {
            log.error("Produit not found dans facture ", ligneDevis.getProduit().getId());
            produitErrors.add("prodtui introvable '");
          }
        }
      }
    }

    if (!produitErrors.isEmpty()) {
      log.warn("");
      throw new InvalidEntityException(
          "prodiot n'existe pas dans la BDD", ErrorCodes.PRODUIT_NOT_FOUND, produitErrors);
    }

    int tauxTva = tvaRepository.getTvaByCode("TVA").getTva();
    double timbre = timbreFiscalService.getTimbreFiscale().getMontant();
    devis.setTauxTVA(tauxTva);
    devis.setReference("aa100");
    devis.setTimbreFiscale(timbre);
    Devis saveDevis = devisRepository.save(devis);

    double montantTotalProduit = 0.0;
    if (devis.getLigneDevis() != null) {
      for (LigneDevis ligneDevis : devis.getLigneDevis()) {
        int remise = ligneDevis.getRemise();
        double montantProduit = ligneDevis.getProduit().getPrix() * ligneDevis.getQuantite();
        if (ligneDevis.getProduit().getEtatRemise() == true) {
          montantTotalProduit =
              montantTotalProduit + (montantProduit - (montantProduit * (remise / 100.0)));
        } else {
          montantTotalProduit = montantTotalProduit + montantProduit;
        }
        ligneDevis.setDevis(saveDevis);
        ligneDevis.setPrixUnitaire(ligneDevis.getProduit().getPrix());
        ligneDevis.setRemise(remise);
        ligneDevis.setPrixTotal((montantProduit - (montantProduit * (remise / 100.0))));
        ligneDevisRepository.save(ligneDevis);
      }
    }
    double montantTotal =
        montantTotalProduit + (montantTotalProduit * (tauxTva / 100.0)) + (timbre / 1000);
    devisRepository.updateMontantTotal(saveDevis.getId(), montantTotalProduit, montantTotal);
    return saveDevis;
  }

  @Override
  public String generateReference() {
    return null;
  }

  @Override
  public Page<DevisDto> findAll(
      Pageable pageable,
      String refdevis,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin) {
    return devisRepository.findAllDevisDto(
        pageable,
        refdevis,
        minMontatnTTC,
        maxMontatnTTC,
        paymentStatus,
        idClient,
        dateDebut,
        dateFin);
  }

  @Override
  public DevisDto findById(Long id) {
    return devisRepository.findDevisById(id);
  }

  @Override
  public ResponseEntity<InputStreamResource> generatePdf(List<Long> ids)
      throws DocumentException, IOException {
    return null;
  }

  @Override
  public ResponseEntity<Void> updateStatus(Long id) {
    return null;
  }

  @Override
  public List<Long> findAllIds(
      String refDevis,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin) {
    return null;
  }

  @Override
  public ResponseEntity<Void> deleteDevis(Long id) {
    return null;
  }
}
