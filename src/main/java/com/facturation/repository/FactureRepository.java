package com.facturation.repository;

import com.facturation.model.Facture;
import com.facturation.model.projection.RecapClient;
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
import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {

  @Transactional
  @Modifying
  @Query(
      "UPDATE Facture f "
          + "SET f.montantHt = :montantHt,"
          + "f.montantTTC = :montantTTC"
          + " WHERE f.id = :idFacture")
  void updateMontantTotal(
      @Param("idFacture") Long idFacture,
      @Param("montantHt") double montantHt,
      @Param("montantTTC") double montantTTC);

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

  @Query(
      "SELECT f FROM Facture f WHERE "
          + "(f.reference = :refFacture OR :refFacture IS NULL)"
          + "and (f.montantTTC >= :minMontatnTTC or :minMontatnTTC is null )"
          + "and (f.montantTTC <= :maxMontatnTTC or :maxMontatnTTC is null )"
          + "and (f.paymentStatus = :paymentStatus or :paymentStatus is null)"
          + "and (f.client.id = :idClient or :idClient is null)"
          + "and (f.dateFacture >= :dateDebut or :dateDebut is null)"
          + "and (f.dateFacture <= :dateFin or :dateFin is null)")
  Page<Facture> findAllFiltre(
      Pageable pageable,
      String refFacture,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin);

  @Query(
      "SELECT f.id FROM Facture f WHERE "
          + "(f.reference = :refFacture OR :refFacture IS NULL)"
          + "and (f.montantTTC >= :minMontatnTTC or :minMontatnTTC is null )"
          + "and (f.montantTTC <= :maxMontatnTTC or :maxMontatnTTC is null )"
          + "and (f.paymentStatus = :paymentStatus or :paymentStatus is null)"
          + "and (f.client.id = :idClient or :idClient is null)"
          + "and (f.dateFacture >= :dateDebut or :dateDebut is null)"
          + "and (f.dateFacture <= :dateFin or :dateFin is null)")
  List<Long> findAllIds(
      String refFacture,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin);

  @Query(
      value =
          "SELECT"
              + "(select sum(facturation.facture.montantttc) FROM facturation.facture where facturation.facture.payment_status = 1) as montantFacturePaye,"
              + "(select sum(facturation.facture.montantttc) FROM facturation.facture where facturation.facture.payment_status = 0) as montantFactureNonPaye,"
              + "(select count(*) FROM facturation.facture where facturation.facture.payment_status = 1) as nbFacturePaye,"
              + "(select count(*) FROM facturation.facture where facturation.facture.payment_status = 0) as nbFactureNonPaye,"
              + "(select count(*) FROM facturation.facture where facturation.facture.payment_status = 0) as nbFactureNonPaye,"
              + "(select sum(facturation.devis.montantttc) FROM facturation.devis where facturation.devis.payment_status = 1) as montantDevisPaye,"
              + "(select sum(facturation.devis.montantttc) FROM facturation.devis where facturation.devis.payment_status = 0) as montantDevisNonPaye,"
              + "(select count(*) FROM facturation.devis where facturation.devis.payment_status = 1) as nbDevisPaye,"
              + "(select count(*) FROM facturation.devis where facturation.devis.payment_status = 0) as nbDevisNonPaye,"
              + "(select count(*) FROM facturation.devis where facturation.devis.payment_status = 0) as nbDevisNonPaye,"
              + "(select count(*) FROM facturation.client) as nbClient",
      nativeQuery = true)
  Statistique getStatistique();

  @Query(
      value =
          "SELECT distinct facturation.client.id AS idClient,"
              + "  facturation.client.nom AS nomClient,"
              + "  facturation.client.prenom AS prenomClient,"
              + "  facturation.client.nom_commercial AS nomCommercial,"
              + "  (SELECT COUNT(*) FROM facturation.facture WHERE facturation.facture.client_id = facturation.client.id) AS numFacture,"
              + "  (SELECT sum(montantttc) FROM facturation.facture WHERE facture.payment_status = 0 ANd facture.client_id = facturation.client.id) AS nmontantNonPaye,"
              + "  (SELECT sum(montantttc) FROM facturation.facture WHERE facture.payment_status = 1 ANd facture.client_id = facturation.client.id) AS nmontantPaye"
              + " FROM facturation.client "
              + "LEFT JOIN"
              + "  facturation.facture ON facturation.client.id = facturation.facture.client_id",
      countQuery = "SELECT count(*) FROM facturation.client",
      nativeQuery = true)
  Page<RecapClient> getRecapClient(Pageable pageable);

  @Query("select fact from Facture fact where fact.id IN  :ids")
  List<Facture> findFactureToPdf(List<Long> ids);
}
