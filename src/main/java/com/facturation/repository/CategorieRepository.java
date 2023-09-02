package com.facturation.repository;

import com.facturation.dto.CategorieDto;
import com.facturation.model.Categorie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

  Optional<Categorie> findByNom(String nom);

  @Query("SELECT NEW com.facturation.dto.CategorieDto(c.id, c.nom, c.description) FROM Categorie c")
  Page<CategorieDto> findAllCategoriesAsDto(Pageable pageable);

  @Query("SELECT NEW com.facturation.dto.CategorieDto(c.id, c.nom, c.description) FROM Categorie c")
  List<CategorieDto> findAllCategoriesAsDto();
}
