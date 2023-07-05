package com.facturation.controller;

import com.facturation.controller.api.TimbreFiscaleApi;
import com.facturation.dto.TimbreFiscalDto;
import com.facturation.model.TimbreFiscal;
import com.facturation.service.TimbreFiscalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class TimbreFiscaleController implements TimbreFiscaleApi {

  private TimbreFiscalService timbreFiscalService;

  @Autowired
  public TimbreFiscaleController(TimbreFiscalService timbreFiscalService) {
    this.timbreFiscalService = timbreFiscalService;
  }

  @Override
  public ResponseEntity<HashMap<String, Object>> updateTimbre(Integer montant) {
    return timbreFiscalService.updateTimbre(montant);
  }

  @Override
  public TimbreFiscalDto getTimbre() {
    return timbreFiscalService.getTimbreFiscale();
  }
}
