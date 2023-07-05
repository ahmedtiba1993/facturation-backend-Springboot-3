package com.facturation.service;

import com.facturation.repository.NumFactureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CronService {

  NumFactureRepository numFactureRepository;

  @Autowired
  public CronService(NumFactureRepository numFactureRepository) {
    this.numFactureRepository = numFactureRepository;
  }

  @Scheduled(cron = "0 0 0 1 1 *")
  public void displayText() {
    log.info("cron service update num Facture = 0");
    this.numFactureRepository.updateNumFacture(0);
  }
}
