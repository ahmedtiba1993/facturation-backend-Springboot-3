package com.facturation.controller.api;

import com.facturation.dto.ProduitDto;
import com.facturation.model.Produit;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.facturation.utils.Constants.PRODUIT_ENDPOINT;
public interface ProduitApi {

    @PostMapping(value = PRODUIT_ENDPOINT+"/create" , consumes = MediaType.APPLICATION_JSON_VALUE , produces = MediaType.APPLICATION_JSON_VALUE)
    ProduitDto save(@RequestBody ProduitDto dto);

    @GetMapping(value =PRODUIT_ENDPOINT+ "id/{idProdtuit}" , produces = MediaType.APPLICATION_JSON_VALUE)
    ProduitDto findById(@PathVariable("idProdtuit") Long id);

    @GetMapping(value =PRODUIT_ENDPOINT+ "/all" , produces = MediaType.APPLICATION_JSON_VALUE)
    Page<ProduitDto> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size);

    @DeleteMapping(value =PRODUIT_ENDPOINT+ "/delete/{idProdtuit}" , produces = MediaType.APPLICATION_JSON_VALUE)
    void delete (@PathVariable("idProdtuit") Long id);

}
