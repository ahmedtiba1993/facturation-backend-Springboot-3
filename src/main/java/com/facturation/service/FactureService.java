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
import java.util.Date;

public interface FactureService {

    FactureDto save(FactureDto dto);

    String generateReference(String codeClient);

    Page<FactureDto> findAll(Pageable pageable , String refFacture , Double minMontatnTTC , Double maxMontatnTTC , Boolean paymentStatus , Long idClient , LocalDate dateDebut , LocalDate dateFin);

    FactureDto findById(Long id);

    ResponseEntity<InputStreamResource> generatePdf(Long id) throws DocumentException, IOException;

    ResponseEntity<Void> updateStatus(Long id);

    Statistique getStatistique();

    Page<RecapClient> getRecapClient(Pageable pageable);

    }
