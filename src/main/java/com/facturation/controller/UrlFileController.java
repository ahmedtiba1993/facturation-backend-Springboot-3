package com.facturation.controller;

import com.facturation.controller.api.UrlFileApi;
import com.facturation.dto.FactureDto;
import com.facturation.dto.UrlFileDto;
import com.facturation.service.UrlFileService;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UrlFileController implements UrlFileApi {

    private final UrlFileService urlFileService;

    @Override
    public ResponseEntity<UrlFileDto> create(String type, Long id) {
        return ResponseEntity.ok().body(urlFileService.createUrlFile(id, type));
    }

    @Override
    public ResponseEntity<UrlFileDto> getUrlFile(UUID uuid) {
        return ResponseEntity.ok().body(urlFileService.getUrlFile(uuid));
    }

    @Override
    public ResponseEntity<InputStreamResource> getFacturePdf(Long id, String type) throws DocumentException, IOException {
        return urlFileService.generatePdf(id, type);
    }
}
