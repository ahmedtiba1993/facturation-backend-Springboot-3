package com.facturation.controller.api;

import com.facturation.dto.FactureDto;
import com.facturation.dto.UrlFileDto;
import com.facturation.utils.Constants;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static com.facturation.utils.Constants.URLFILE_ENDPOINT;

public interface UrlFileApi {

    @PostMapping(value = URLFILE_ENDPOINT+"/create/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UrlFileDto> create(String type, Long id);

    @GetMapping(value = URLFILE_ENDPOINT+"/pdf/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE )
    ResponseEntity<FactureDto> getFactureId(@PathVariable UUID uuid);

    @GetMapping(value =URLFILE_ENDPOINT+"/pdf/{factureId}")
    ResponseEntity<InputStreamResource> getFacturePdf(@PathVariable Long factureId) throws DocumentException, IOException;
}
