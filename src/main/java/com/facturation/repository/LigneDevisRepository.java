package com.facturation.repository;

import com.facturation.model.LigneDevis;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LigneDevisRepository extends JpaRepository<LigneDevis, Long> {

  @Modifying
  @Transactional
  @Query(value = "delete from ligne_devis where devis_id = :id", nativeQuery = true)
  void deleteByIdDevis(Long id);

  @Query("SELECT l from LigneDevis l where l.devis.id = :id")
  List<LigneDevis> findByDevisId(Long id);
}
