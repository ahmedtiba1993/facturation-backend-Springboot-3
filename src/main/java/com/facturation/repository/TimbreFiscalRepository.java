package com.facturation.repository;

import com.facturation.model.TimbreFiscal;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TimbreFiscalRepository extends JpaRepository<TimbreFiscal, Long> {

  @Query("SELECT t FROM TimbreFiscal t WHERE t.code LIKE %:code%")
  Optional<TimbreFiscal> findTimbreFiscale(String code);

  @Transactional
  @Modifying
  @Query("update TimbreFiscal set montant =:montant where code = 'TIMBRE'")
  void updateTimbre(Integer montant);
}
