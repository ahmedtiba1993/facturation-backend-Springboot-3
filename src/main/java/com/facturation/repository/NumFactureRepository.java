package com.facturation.repository;

import com.facturation.model.NumFacture;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NumFactureRepository extends JpaRepository<NumFacture, Long> {

    @Query( value = "SELECT num_facture FROM num_facture LIMIT 1",nativeQuery = true)
    Integer getNumFacture();

    @Transactional
    @Modifying
    @Query( value = "update num_facture set num_facture =:num",nativeQuery = true)
    void updateNumFacture(Integer num);
}
