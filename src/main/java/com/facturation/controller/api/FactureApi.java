package com.facturation.controller.api;

import com.facturation.dto.ClientDto;
import com.facturation.dto.FactureDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.facturation.utils.Constants.FACTURE_ENDPOINT;

public interface FactureApi {

    @PostMapping(value = FACTURE_ENDPOINT+"/create" , consumes = MediaType.APPLICATION_JSON_VALUE , produces = MediaType.APPLICATION_JSON_VALUE)
    FactureDto save(@RequestBody FactureDto dto);
}
