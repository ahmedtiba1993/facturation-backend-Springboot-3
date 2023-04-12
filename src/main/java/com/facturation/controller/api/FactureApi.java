package com.facturation.controller.api;

import com.facturation.dto.ClientDto;
import com.facturation.dto.FactureDto;
import com.facturation.dto.ProduitDto;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static com.facturation.utils.Constants.FACTURE_ENDPOINT;
import static com.facturation.utils.Constants.PRODUIT_ENDPOINT;

public interface FactureApi {

    @PostMapping(value = FACTURE_ENDPOINT+"/create" , consumes = MediaType.APPLICATION_JSON_VALUE , produces = MediaType.APPLICATION_JSON_VALUE)
    FactureDto save(@RequestBody FactureDto dto);

    @GetMapping(value =FACTURE_ENDPOINT+ "/all" , produces = MediaType.APPLICATION_JSON_VALUE)
    Page<FactureDto> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size);

    @GetMapping(value =FACTURE_ENDPOINT+ "/{idFacture}" , produces = MediaType.APPLICATION_JSON_VALUE)
    FactureDto findById(@PathVariable("idFacture") Long id);
}
