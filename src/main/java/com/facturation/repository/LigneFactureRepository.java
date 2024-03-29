package com.facturation.repository;

import com.facturation.model.LigneFacture;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LigneFactureRepository extends JpaRepository<LigneFacture, Long> {

  @Modifying
  @Transactional
  @Query(value = "delete from ligne_facture where facture_id = :id", nativeQuery = true)
  void deleteByIdFacture(Long id);
}
