package com.facturation.controller.api;

import com.facturation.dto.CategorieDto;
import com.facturation.dto.ProduitDto;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.facturation.utils.Constants.CATEGORRIE_ENDPOINT;

public interface CategorieApi {

    @PostMapping(value = CATEGORRIE_ENDPOINT+"/create" , consumes = MediaType.APPLICATION_JSON_VALUE , produces = MediaType.APPLICATION_JSON_VALUE)
    CategorieDto save(@RequestBody CategorieDto dto);

    @GetMapping(value =CATEGORRIE_ENDPOINT+ "/id/{idCat}" , produces = MediaType.APPLICATION_JSON_VALUE)
    CategorieDto findById(@PathVariable("idCat") Long id);

    @GetMapping(value =CATEGORRIE_ENDPOINT+ "/allpaginated" , produces = MediaType.APPLICATION_JSON_VALUE)
    Page<CategorieDto> findAllPaginated(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size);

    @DeleteMapping(value =CATEGORRIE_ENDPOINT+ "/delete/{idCat}" , produces = MediaType.APPLICATION_JSON_VALUE)
    void delete (@PathVariable("idCat") Long id);

    @GetMapping(value =CATEGORRIE_ENDPOINT+ "/all" , produces = MediaType.APPLICATION_JSON_VALUE)
    List<CategorieDto> findAll();

}
