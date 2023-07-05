package com.facturation.service.impl;

import com.facturation.model.Tva;
import com.facturation.repository.TvaRepository;
import com.facturation.service.TvaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class TvaServiceImpl implements TvaService {

  TvaRepository tvaRepository;

  @Autowired
  public TvaServiceImpl(TvaRepository tvaRepository) {
    this.tvaRepository = tvaRepository;
  }

  @Override
  public Tva getTva() {
    return tvaRepository.getTvaByCode("TVA");
  }

  @Override
  public ResponseEntity<HashMap<String, Object>> updateTva(int tva) {

    ResponseEntity<HashMap<String, Object>> response = null;
    HashMap<String, Object> dataRespenseObject = new HashMap<String, Object>();

    List<String> errors = new ArrayList<>();

    if (tva <= 0) {
      dataRespenseObject.put("success", false);
      response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);
      return response;
    }

    tvaRepository.updateTva(tva);

    dataRespenseObject.put("success", true);
    response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);

    return response;
  }
}
