package com.facturation.service;

import com.facturation.model.Tva;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public interface TvaService {

  Tva getTva();

  ResponseEntity<HashMap<String, Object>> updateTva(int tva);
}
