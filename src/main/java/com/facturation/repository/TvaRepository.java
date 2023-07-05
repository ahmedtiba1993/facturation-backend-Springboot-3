package com.facturation.repository;

import com.facturation.model.TimbreFiscal;
import com.facturation.model.Tva;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TvaRepository extends JpaRepository<Tva, Long> {
  Tva getTvaByCode(String code);

  @Transactional
  @Modifying
  @Query("update Tva set tva =:tva where code = 'TVA'")
  void updateTva(int tva);
}
