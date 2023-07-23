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

public interface DevisRepository extends JpaRepository<Devis, Long> {

  @Query(
      "SELECT NEW com.facturation.dto.DevisDto("
          + "d.id, d.dateDevis, d.tauxTVA, d.montantTTC, d.montantHt, d.reference, d.timbreFiscale, d.paymentStatus, "
          + "d.client.id, d.client.nom, d.client.prenom) "
          + "FROM Devis d WHERE "
          + "(d.reference = :refDevis OR :refDevis IS NULL)"
          + "and (d.montantTTC >= :minMontatnTTC or :minMontatnTTC is null )"
          + "and (d.montantTTC <= :maxMontatnTTC or :maxMontatnTTC is null )"
          + "and (d.paymentStatus = :paymentStatus or :paymentStatus is null)"
          + "and (d.client.id = :idClient or :idClient is null)"
          + "and (d.dateDevis >= :dateDebut or :dateDebut is null)"
          + "and (d.dateDevis <= :dateFin or :dateFin is null)")
  Page<DevisDto> findAllDevisDto(
      Pageable pageable,
      String refDevis,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin);

  @Query(
      value =
          "SELECT d.id AS id, d.date_devis AS dateDevis, d.taux_tva AS tauxTVA, "
              + "d.montant_ttc AS montantTTC, d.montant_ht AS montantHt, "
              + "d.reference AS reference, d.timbre_fiscale AS timbreFiscale, "
              + "d.payment_status AS paymentStatus "
              + "FROM devis d WHERE d.id = ?1",
      nativeQuery = true)
  DevisDto findDevisById(Long id);

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
}
