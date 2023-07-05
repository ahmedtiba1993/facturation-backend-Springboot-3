package com.facturation.service;

import com.facturation.dto.TimbreFiscalDto;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public interface TimbreFiscalService {

  TimbreFiscalDto ajouter(TimbreFiscalDto dto);

  TimbreFiscalDto getTimbreFiscale();

  ResponseEntity<HashMap<String, Object>> updateTimbre(Integer montant);
}
