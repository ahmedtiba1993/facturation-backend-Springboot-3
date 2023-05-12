package com.facturation.service;

import com.facturation.dto.FactureDto;
import com.facturation.dto.ProduitDto;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface FactureService {

    FactureDto save(FactureDto dto);

    String generateReference(String codeClient);

    Page<FactureDto> findAll(Pageable pageable);

    FactureDto findById(Long id);

    ResponseEntity<InputStreamResource> generatePdf(Long id) throws DocumentException, IOException;

    ResponseEntity<Void> updateStatus(Long id);

    }
