package com.facturation.controller.api;

import com.facturation.model.Tva;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

import static com.facturation.utils.Constants.TVA_ENDPOINT;

public interface TvaApi {

  @PostMapping(value = TVA_ENDPOINT + "/updateTva", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<HashMap<String, Object>> updateTva(@RequestParam int tva);

  @GetMapping(value = TVA_ENDPOINT + "/getTva", produces = MediaType.APPLICATION_JSON_VALUE)
  Tva getTva();
}
