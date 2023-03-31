package com.facturation.repository;

import com.facturation.model.LigneFacture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneFactureRepository extends JpaRepository<LigneFacture, Long> {
}
