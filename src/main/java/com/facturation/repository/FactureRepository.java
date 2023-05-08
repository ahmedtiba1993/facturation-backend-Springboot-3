package com.facturation.repository;

import com.facturation.model.Facture;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Facture f " +
            "SET f.montantHt = :montantHt," +
            "f.montantTTC = :montantTTC" +
            " WHERE f.id = :idFacture")
    void updateMontantTotal(@Param("idFacture") Long idFacture,
                            @Param("montantHt") double montantHt,
                            @Param("montantTTC") double montantTTC
                            );

    @Query("SELECT COUNT(f) FROM Facture f JOIN f.client c WHERE c.code LIKE %:codeClient%")
    int countByClient_Code(@Param("codeClient") String codeClient);

    @Query("SELECT COUNT(f) FROM Facture f WHERE YEAR(f.dateFacture) = :currentYear")
    int countByYers(@Param("currentYear") int currentYear);


}
