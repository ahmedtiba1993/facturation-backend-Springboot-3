package com.facturation.controller;

import com.facturation.controller.api.FactureApi;
import com.facturation.dto.FactureDto;
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
    public Page<FactureDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return factureService.findAll(pageable);    }

    @Override
    public FactureDto findById(Long id) {
        return factureService.findById(id);
    }

    @Override
    public ResponseEntity<InputStreamResource> generatePdf(Long id) throws DocumentException, IOException {
      return factureService.generatePdf(id);
    }

    @Override
    public ResponseEntity<Void> updateStatut(Long id) {
        return factureService.updateStatus(id);
    }
}
