package com.facturation.repository;

import com.facturation.model.TimbreFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TimbreFiscalRepository extends JpaRepository<TimbreFiscal, Long> {

    @Query("SELECT t FROM TimbreFiscal t WHERE t.code LIKE %:code%")
    Optional<TimbreFiscal> findTimbreFiscale(String code);

}
