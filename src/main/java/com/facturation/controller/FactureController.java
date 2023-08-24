package com.facturation.controller;

import com.facturation.controller.api.FactureApi;
import com.facturation.dto.FactureDto;
import com.facturation.model.projection.RecapClient;
import com.facturation.model.projection.Statistique;
import com.facturation.service.FactureService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
public class FactureController implements FactureApi {

  private FactureService factureService;

  @Autowired
  public FactureController(FactureService factureService) {
    this.factureService = factureService;
  }

  @Override
  public FactureDto save(FactureDto dto) {
    return factureService.save(dto);
  }

  @Override
  public Page<FactureDto> findAll(
      int page,
      int size,
      String refFacture,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
    if (refFacture != null && refFacture.equals("")) {
      refFacture = null;
    }
    return factureService.findAll(
        pageable,
        refFacture,
        minMontatnTTC,
        maxMontatnTTC,
        paymentStatus,
        idClient,
        dateDebut,
        dateFin);
  }

  @Override
  public FactureDto findById(Long id) {
    return factureService.findById(id);
  }

  @Override
  public ResponseEntity<InputStreamResource> generatePdf(List<Long> ids)
      throws DocumentException, IOException {
    return factureService.generatePdf(ids);
  }

  @Override
  public ResponseEntity<Void> updateStatut(Long id) {
    return factureService.updateStatus(id);
  }

  @Override
  public Statistique getStatistique() {
    return factureService.getStatistique();
  }

  @Override
  public Page<RecapClient> getRecapClient(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return factureService.getRecapClient(pageable);
  }

  @Override
  public List<Long> findAllIds(
      String refFacture,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin) {
    return factureService.findAllIds(
        refFacture, minMontatnTTC, maxMontatnTTC, paymentStatus, idClient, dateDebut, dateFin);
  }

  @Override
  public ResponseEntity<Void> deleteFacture(Long id) {
    return factureService.deleteFacture(id);
  }

  @Override
  public ResponseEntity<Void> deleteLigneFacture(Long idFacture, Long idLigneFacture) {
    return factureService.deleteLingeFacture(idFacture, idLigneFacture);
  }

  @Override
  public ResponseEntity<Void> ajouterLigneFacture(
      Long factureId, Long idProduit, Double prix, Integer quantite, Integer remise) {
    return factureService.ajouterLingeFacture(factureId, idProduit, prix, quantite, remise);
  }
}
