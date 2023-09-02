package com.facturation.service.impl;

import com.facturation.model.NumFacture;
import com.facturation.repository.NumFactureRepository;
import com.facturation.service.NumFactureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class numFactureServiceImpl implements NumFactureService {

  private NumFactureRepository numFactureRepository;

  @Autowired
  public numFactureServiceImpl(NumFactureRepository numFactureRepository) {
    this.numFactureRepository = numFactureRepository;
  }

  @Override
  public ResponseEntity<HashMap<String, Object>> updateNumFacture(Integer numFacture) {

    ResponseEntity<HashMap<String, Object>> response = null;
    HashMap<String, Object> dataRespenseObject = new HashMap<String, Object>();

    if (numFacture <= 0) {
      dataRespenseObject.put("success", false);
      response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);
      return response;
    }

    numFactureRepository.updateNumFacture(numFacture);

    dataRespenseObject.put("success", true);
    response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);
    return response;
  }

  @Override
  public ResponseEntity<HashMap<String, Object>> updateNumDevis(Integer numDevis) {
    ResponseEntity<HashMap<String, Object>> response = null;
    HashMap<String, Object> dataRespenseObject = new HashMap<String, Object>();

    if (numDevis <= 0) {
      dataRespenseObject.put("success", false);
      response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);
      return response;
    }

    numFactureRepository.updateNumDevis(numDevis);

    dataRespenseObject.put("success", true);
    response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);
    return response;
  }

  @Override
  public NumFacture getNumFacture() {
    return numFactureRepository.getNumberFacture();
  }
}
