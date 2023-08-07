package com.facturation.repository;

import com.facturation.dto.DevisDto;
import com.facturation.model.Devis;
import com.facturation.model.Facture;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DevisRepository extends JpaRepository<Devis, Long> {

  @Query(
      "SELECT d "
          + "FROM Devis d WHERE "
          + "(d.reference = :refDevis OR :refDevis IS NULL)"
          + "and (d.montantTTC >= :minMontatnTTC or :minMontatnTTC is null )"
          + "and (d.montantTTC <= :maxMontatnTTC or :maxMontatnTTC is null )"
          + "and (d.paymentStatus = :paymentStatus or :paymentStatus is null)"
          + "and (d.client.id = :idClient or :idClient is null)"
          + "and (d.dateDevis >= :dateDebut or :dateDebut is null)"
          + "and (d.dateDevis <= :dateFin or :dateFin is null)")
  Page<Devis> findAllFiltre(
      Pageable pageable,
      String refDevis,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin);

  @Transactional
  @Modifying
  @Query(
      "UPDATE Devis d "
          + "SET d.montantHt = :montantHt,"
          + "d.montantTTC = :montantTTC"
          + " WHERE d.id = :idDevis")
  void updateMontantTotal(
      @Param("idDevis") Long idDevis,
      @Param("montantHt") double montantHt,
      @Param("montantTTC") double montantTTC);

  @Query(
      "SELECT d "
          + "FROM Devis d "
          + "LEFT JOIN d.client c "
          + "LEFT JOIN d.ligneDevis ld "
          + "WHERE d.id = :devisId")
  DevisDto findDevisDtoById(@Param("devisId") Long devisId);

  @Query("select d from Devis d where d.id IN  :ids")
  List<Devis> findDevisToPdf(List<Long> ids);

  @Modifying
  @Transactional
  @Query("UPDATE Devis d SET d.paymentStatus = true WHERE d.id = :id")
  void setStatusTrue(Long id);

  @Modifying
  @Transactional
  @Query("UPDATE Devis d SET d.paymentStatus = false WHERE d.id = :id")
  void setStatusFalse(Long id);

  @Query(
      "SELECT f.id FROM Devis f WHERE "
          + "(f.reference = :refFacture OR :refFacture IS NULL)"
          + "and (f.montantTTC >= :minMontatnTTC or :minMontatnTTC is null )"
          + "and (f.montantTTC <= :maxMontatnTTC or :maxMontatnTTC is null )"
          + "and (f.paymentStatus = :paymentStatus or :paymentStatus is null)"
          + "and (f.client.id = :idClient or :idClient is null)"
          + "and (f.dateDevis >= :dateDebut or :dateDebut is null)"
          + "and (f.dateDevis <= :dateFin or :dateFin is null)")
  List<Long> findAllIds(
      String refFacture,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin);
}
