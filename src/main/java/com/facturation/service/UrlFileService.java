package com.facturation.service;

import com.facturation.dto.FactureDto;
import com.facturation.dto.UrlFileDto;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface UrlFileService {

    UrlFileDto createUrlFile(Long id, String type);

    FactureDto getFactureId(UUID uuid);

    ResponseEntity<InputStreamResource> generatePdf(Long id)
            throws DocumentException, IOException;
}
