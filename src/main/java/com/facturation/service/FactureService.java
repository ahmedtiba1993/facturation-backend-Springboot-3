package com.facturation.service;

import com.facturation.dto.FactureDto;
import com.facturation.dto.ProduitDto;
import com.facturation.model.projection.RecapClient;
import com.facturation.model.projection.Statistique;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface FactureService {

  FactureDto save(FactureDto dto);

  String generateReference(String codeClient);

  Page<FactureDto> findAll(
      Pageable pageable,
      String refFacture,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin);

  FactureDto findById(Long id);

  ResponseEntity<InputStreamResource> generatePdf(List<Long> ids)
      throws DocumentException, IOException;

  ResponseEntity<Void> updateStatus(Long id);

  Statistique getStatistique();

  Page<RecapClient> getRecapClient(Pageable pageable);

  List<Long> findAllIds(
      String refFacture,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin);

  ResponseEntity<Void> deleteFacture(Long id);

  ResponseEntity<Void> deleteLingeFacture(Long factureId, Long ligneFactureId);

  ResponseEntity<Void> ajouterLingeFacture(
      Long factureId, Long idProduit, double prix, Integer quatite, Integer remise);

  Long creationDevis(Long factureId);

  Long creationBonLivraison(Long factureId);

}
