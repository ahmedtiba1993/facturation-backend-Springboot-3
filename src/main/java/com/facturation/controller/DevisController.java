package com.facturation.controller;

import com.facturation.controller.api.DevisApi;
import com.facturation.dto.DevisDto;
import com.facturation.model.Devis;
import com.facturation.service.DevisService;
import com.itextpdf.text.DocumentException;
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

  private DevisService devisService;

  @Autowired
  public DevisController(DevisService devisService) {
    this.devisService = devisService;
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
}
