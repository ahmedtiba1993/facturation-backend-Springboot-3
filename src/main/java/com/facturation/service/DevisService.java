package com.facturation.service;

import com.facturation.dto.DevisDto;
import com.facturation.model.Devis;
import com.facturation.model.projection.ClientRecapProjection;
import com.facturation.model.projection.RecapClient;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DevisService {

  DevisDto save(Devis devis);

  String generateReference();

  Page<DevisDto> findAll(
      Pageable pageable,
      String refdevis,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin);

  DevisDto findById(Long id);

  ResponseEntity<InputStreamResource> generatePdf(List<Long> ids)
      throws DocumentException, IOException;

  ResponseEntity<Void> updateStatus(Long id);

  List<Long> findAllIds(
      String refDevis,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin);

  ResponseEntity<Void> deleteDevis(Long id);

  Page<ClientRecapProjection> getRecapClient(Pageable pageable);

  ResponseEntity<Void> createFactureFromDevis(Long id);
}
