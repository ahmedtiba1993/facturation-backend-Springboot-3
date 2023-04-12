package com.facturation.controller.api;

import com.facturation.dto.ClientDto;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.facturation.utils.Constants.CLIENT_ENDPOINT;

public interface ClientApi {

    @PostMapping(value = CLIENT_ENDPOINT+"/create" , consumes = MediaType.APPLICATION_JSON_VALUE , produces = MediaType.APPLICATION_JSON_VALUE)
    ClientDto save(@RequestBody ClientDto dto);

    @GetMapping(value =CLIENT_ENDPOINT+ "/id/{idClient}" , produces = MediaType.APPLICATION_JSON_VALUE)
    ClientDto findById(@PathVariable("idClient") Long id);

    @GetMapping(value =CLIENT_ENDPOINT+ "/allpaginated" , produces = MediaType.APPLICATION_JSON_VALUE)
    Page<ClientDto> findAllPaginated(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size);

    @DeleteMapping(value =CLIENT_ENDPOINT+ "/delete/{idClient}" , produces = MediaType.APPLICATION_JSON_VALUE)
    void delete (@PathVariable("idClient") Long id);

    @GetMapping(value =CLIENT_ENDPOINT+ "/all" , produces = MediaType.APPLICATION_JSON_VALUE)
    List<ClientDto> findAll();
}
