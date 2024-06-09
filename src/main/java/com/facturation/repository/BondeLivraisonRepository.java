package com.facturation.repository;

import com.facturation.model.BondeLivraison;
import com.facturation.model.Devis;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BondeLivraisonRepository extends JpaRepository<BondeLivraison, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE BondeLivraison d " + "SET d.montantHt = :montantHt," + "d.montantTTC = :montantTTC" + " WHERE d.id = :idBondLivraison")
    void updateMontantTotal(@Param("idBondLivraison") Long idBondLivraison, @Param("montantHt") double montantHt, @Param("montantTTC") double montantTTC);

    @Query(
            "SELECT d "
                    + "FROM BondeLivraison d WHERE "
                    + "(d.reference = :refBondeLivraison OR :refBondeLivraison IS NULL)"
                    + "and (d.montantTTC >= :minMontatnTTC or :minMontatnTTC is null )"
                    + "and (d.montantTTC <= :maxMontatnTTC or :maxMontatnTTC is null )"
                    + "and (d.client.id = :idClient or :idClient is null)"
                    + "and (d.dateBondeLivraison >= :dateDebut or :dateDebut is null)"
                    + "and (d.dateBondeLivraison <= :dateFin or :dateFin is null)")
    Page<BondeLivraison> findAllFiltre(
            Pageable pageable,
            String refBondeLivraison,
            Double minMontatnTTC,
            Double maxMontatnTTC,
            Long idClient,
            LocalDate dateDebut,
            LocalDate dateFin);

    @Query("select d from BondeLivraison d where d.id IN  :ids")
    List<BondeLivraison> findBondeLivraisonToPdf(List<Long> ids);

   @Query(
            "SELECT f.id FROM BondeLivraison f WHERE "
                    + "(f.reference = :refBondeLivraison OR :refBondeLivraison IS NULL)"
                    + "and (f.montantTTC >= :minMontatnTTC or :minMontatnTTC is null )"
                    + "and (f.montantTTC <= :maxMontatnTTC or :maxMontatnTTC is null )"
                    + "and (f.client.id = :idClient or :idClient is null)"
                    + "and (f.dateBondeLivraison >= :dateDebut or :dateDebut is null)"
                    + "and (f.dateBondeLivraison <= :dateFin or :dateFin is null)")
    List<Long> findAllIds(String refBondeLivraison, Double minMontatnTTC, Double maxMontatnTTC, Long idClient, LocalDate dateDebut, LocalDate dateFin);


    @Modifying
    @Transactional
    @Query("UPDATE BondeLivraison d SET d.montantHt = :montantHt,d.montantTTC = :montantTTC WHERE d.id = :id")
    void updateMontant(Long id, Double montantHt, Double montantTTC);
}
