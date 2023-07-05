package com.facturation.controller;

import com.facturation.controller.api.TvaApi;
import com.facturation.model.Tva;
import com.facturation.service.TvaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class TvaController implements TvaApi {

  private TvaService tvaService;

  @Autowired
  public TvaController(TvaService tvaService) {
    this.tvaService = tvaService;
  }

  @Override
  public ResponseEntity<HashMap<String, Object>> updateTva(int tva) {
    return tvaService.updateTva(tva);
  }

  @Override
  public Tva getTva() {
    return tvaService.getTva();
  }
}
