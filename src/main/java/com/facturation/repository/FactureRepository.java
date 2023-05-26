package com.facturation.repository;

import com.facturation.model.Facture;
import com.facturation.model.projection.Statistique;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;

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

    @Modifying
    @Transactional
    @Query("UPDATE Facture f SET f.paymentStatus = true WHERE f.id = :id")
    void setStatusTrue(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Facture f SET f.paymentStatus = false WHERE f.id = :id")
    void setStatusFalse(Long id);
    @Query("SELECT f FROM Facture f WHERE " +
            "(f.reference = :refFacture OR :refFacture IS NULL)" +
            "and (f.montantTTC >= :minMontatnTTC or :minMontatnTTC is null )" +
            "and (f.montantTTC <= :maxMontatnTTC or :maxMontatnTTC is null )" +
            "and (f.paymentStatus = :paymentStatus or :paymentStatus is null)" +
            "and (f.client.id = :idClient or :idClient is null)" +
            "and (f.dateFacture >= :dateDebut or :dateDebut is null)" +
            "and (f.dateFacture <= :dateFin or :dateFin is null)")
    Page<Facture> findAllFiltre(Pageable pageable , String refFacture , Double minMontatnTTC , Double maxMontatnTTC , Boolean paymentStatus , Long idClient , LocalDate dateDebut , LocalDate dateFin);


    @Query(value = "SELECT" +
            "(select sum(facturation.facture.montantttc) FROM facturation.facture where facturation.facture.payment_status = 1) as montatPaye," +
            "(select sum(facturation.facture.montantttc) FROM facturation.facture where facturation.facture.payment_status = 0) as montantNonPaye," +
            "(select count(*) FROM facturation.facture where facturation.facture.payment_status = 1) as nbFacturePaye," +
            "(select count(*) FROM facturation.facture where facturation.facture.payment_status = 0) as nbFactureNonPaye," +
            "(select count(*) FROM facturation.facture) as nbClient",nativeQuery = true)
    Statistique getStatistique();

}
