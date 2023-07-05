package com.facturation.controller.api;

import com.facturation.model.NumFacture;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

import static com.facturation.utils.Constants.NUM_FACTURE_ENDPOINT;

public interface NumFactureApi {

  @PostMapping(
      value = NUM_FACTURE_ENDPOINT + "/getNumFacture",
      produces = MediaType.APPLICATION_JSON_VALUE)
  NumFacture getNumFacture();

  @GetMapping(
      value = NUM_FACTURE_ENDPOINT + "/updateNumFacture",
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<HashMap<String, Object>> updateNumFacture(@RequestParam Integer numFacture);
}
