package com.facturation.repository;

import com.facturation.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    @Query("SELECT COUNT(p) FROM Produit p WHERE p.categorie.id = :categorieId")
    Long countByCategorieId(Long categorieId);


}
