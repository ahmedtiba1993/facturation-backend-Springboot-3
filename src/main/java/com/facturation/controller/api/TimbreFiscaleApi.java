package com.facturation.controller.api;

import com.facturation.dto.TimbreFiscalDto;
import com.facturation.model.TimbreFiscal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

import static com.facturation.utils.Constants.TIMBRE_ENDPOINT;

public interface TimbreFiscaleApi {

  @PostMapping(
      value = TIMBRE_ENDPOINT + "/updateTimbre",
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<HashMap<String, Object>> updateTimbre(@RequestParam Integer montant);

  @GetMapping(value = TIMBRE_ENDPOINT + "/getTimbre", produces = MediaType.APPLICATION_JSON_VALUE)
  TimbreFiscalDto getTimbre();
}
