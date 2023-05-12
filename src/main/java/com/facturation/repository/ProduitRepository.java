package com.facturation.repository;

import com.facturation.model.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    @Query("SELECT COUNT(p) FROM Produit p WHERE p.categorie.id = :categorieId")
    Long countByCategorieId(Long categorieId);

    Optional<Produit> findProduitByCode(String code);

    @Query(value = "SELECT p FROM Produit p " +
            "WHERE (:nom IS NULL OR p.nom LIKE %:nom%) " +
            "AND (:code IS NULL OR p.code = :code) " +
            "AND (:prixMin IS NULL OR p.prix >= :prixMin) " +
            "AND (:prixMax IS NULL OR p.prix <= :prixMax) " +
            "AND (:etatRemise IS NULL OR p.etatRemise = :etatRemise)")
    Page<Produit> findAllProduitByFiltre(Pageable pageable , String nom, String code, Double prixMin, Double prixMax, Boolean etatRemise);

}
