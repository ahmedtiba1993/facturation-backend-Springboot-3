package com.facturation.controller;

import com.facturation.controller.api.DevisApi;
import com.facturation.dto.DevisDto;
import com.facturation.model.Devis;
import com.facturation.model.projection.ClientRecapProjection;
import com.facturation.service.DevisService;
import com.facturation.service.EmailService;
import com.itextpdf.text.DocumentException;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
public class DevisController implements DevisApi {

  private final EmailService emailService;
  private DevisService devisService;

  @Autowired
  public DevisController(DevisService devisService, EmailService emailService) {
    this.devisService = devisService;
    this.emailService = emailService;
  }

  @Override
  public DevisDto save(Devis devis) {
    return devisService.save(devis);
  }

  @Override
  public Page<DevisDto> findAll(
      int page,
      int size,
      String refDevis,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    if (refDevis != null && refDevis.equals("")) {
      refDevis = null;
    }
    return devisService.findAll(
        pageable,
        refDevis,
        minMontatnTTC,
        maxMontatnTTC,
        paymentStatus,
        idClient,
        dateDebut,
        dateFin);
  }

  @Override
  public DevisDto findById(Long id) {
    return devisService.findById(id);
  }

  @Override
  public ResponseEntity<InputStreamResource> generatePdf(List<Long> ids)
      throws DocumentException, IOException {
    return devisService.generatePdf(ids);
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
    return devisService.findAllIds(
        refDevis, minMontatnTTC, maxMontatnTTC, paymentStatus, idClient, dateDebut, dateFin);
  }

  @Override
  public ResponseEntity<Void> updateStatut(Long id) {
    return devisService.updateStatus(id);
  }

  @Override
  public ResponseEntity<Void> deleteDevis(Long id) {
    return devisService.deleteDevis(id);
  }

  @Override
  public ResponseEntity<Void> convertDevisToFacture(Long id) {
    return devisService.createFactureFromDevis(id);
  }

  @Override
  public Page<ClientRecapProjection> getRecapClient(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return devisService.getRecapClient(pageable);
  }

  @Override
  public ResponseEntity<Void> deleteLignedevis(Long idDevis, Long idLigneDevis) {
    return devisService.deleteLingeDevis(idDevis, idLigneDevis);
  }

  @Override
  public ResponseEntity<Void> ajouterLigneDevis(
      Long factureId, Long idProduit, Double prix, Integer quantite, Integer remise) {
    return devisService.ajouterLingeDevis(factureId, idProduit, prix, quantite, remise);
  }

  @Override
  public ResponseEntity<Long> creationBonLivraison(Long devisId) {
    return ResponseEntity.ok().body(devisService.creationBonLivraison(devisId));
  }

  @Override
  public ResponseEntity<Void> sendMail(Long devisId) throws DocumentException, IOException, MessagingException {
    emailService.sendEmailDevis(Long.valueOf(devisId) );
    return ResponseEntity.ok().build();
  }
}
