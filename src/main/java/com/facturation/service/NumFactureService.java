package com.facturation.service;

import com.facturation.model.NumFacture;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public interface NumFactureService {

  ResponseEntity<HashMap<String, Object>> updateNumFacture(Integer numFacture);

  NumFacture getNumFacture();
}
