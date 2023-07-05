package com.facturation.controller;

import com.facturation.controller.api.NumFactureApi;
import com.facturation.model.NumFacture;
import com.facturation.service.NumFactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class NumFactureController implements NumFactureApi {

  private NumFactureService numFactureService;

  @Autowired
  public NumFactureController(NumFactureService numFactureService) {
    this.numFactureService = numFactureService;
  }

  @Override
  public NumFacture getNumFacture() {
    return numFactureService.getNumFacture();
  }

  @Override
  public ResponseEntity<HashMap<String, Object>> updateNumFacture(Integer numFacture) {
    return this.numFactureService.updateNumFacture(numFacture);
  }
}
