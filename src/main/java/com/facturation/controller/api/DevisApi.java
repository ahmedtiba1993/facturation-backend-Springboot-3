package com.facturation.controller.api;

import com.facturation.dto.DevisDto;
import com.facturation.dto.FactureDto;
import com.facturation.model.Devis;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static com.facturation.utils.Constants.DEVIS_ENDPOINT;
import static com.facturation.utils.Constants.FACTURE_ENDPOINT;

public interface DevisApi {

  @PostMapping(
      value = DEVIS_ENDPOINT + "/create",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DevisDto save(@RequestBody Devis dto);

  @GetMapping(value = DEVIS_ENDPOINT + "/all", produces = MediaType.APPLICATION_JSON_VALUE)
  Page<DevisDto> findAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "3") int size,
      @RequestParam(required = false) String refDevis,
      @RequestParam(required = false) Double minMontatnTTC,
      @RequestParam(required = false) Double maxMontatnTTC,
      @RequestParam(required = false) Boolean paymentStatus,
      @RequestParam(required = false) Long idClient,
      @RequestParam(required = false) LocalDate dateDebut,
      @RequestParam(required = false) LocalDate dateFin);

  @GetMapping(value = DEVIS_ENDPOINT + "/{idDevis}", produces = MediaType.APPLICATION_JSON_VALUE)
  DevisDto findById(@PathVariable("idDevis") Long id);

  @GetMapping(value = DEVIS_ENDPOINT + "/generate-pdf")
  ResponseEntity<InputStreamResource> generatePdf(@RequestParam List<Long> ids)
      throws DocumentException, IOException;

  @GetMapping(value = DEVIS_ENDPOINT + "/allIds", produces = MediaType.APPLICATION_JSON_VALUE)
  List<Long> findAllIds(
      @RequestParam(required = false) String refDevis,
      @RequestParam(required = false) Double minMontatnTTC,
      @RequestParam(required = false) Double maxMontatnTTC,
      @RequestParam(required = false) Boolean paymentStatus,
      @RequestParam(required = false) Long idClient,
      @RequestParam(required = false) LocalDate dateDebut,
      @RequestParam(required = false) LocalDate dateFin);

  @PostMapping(
      value = DEVIS_ENDPOINT + "/statutupdate/{idDevis}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<Void> updateStatut(@PathVariable("idDevis") Long id);
}
