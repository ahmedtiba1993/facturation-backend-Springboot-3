package com.facturation.service;

import com.facturation.model.NumFacture;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public interface NumFactureService {

  ResponseEntity<HashMap<String, Object>> updateNumFacture(Integer numFacture);

  ResponseEntity<HashMap<String, Object>> updateNumDevis(Integer numDevis);

  NumFacture getNumFacture();
}
