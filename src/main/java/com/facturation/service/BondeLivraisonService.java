package com.facturation.service;

import com.facturation.dto.BondeLivraisonDto;
import com.facturation.model.BondeLivraison;
import com.facturation.model.projection.ClientRecapProjection;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface BondeLivraisonService {

    BondeLivraisonDto save(BondeLivraison bondeLivraison);

    Page<BondeLivraisonDto> findAll(
            Pageable pageable,
            String refBondeLivraison,
            Double minMontatnTTC,
            Double maxMontatnTTC,
            Long idClient,
            LocalDate dateDebut,
            LocalDate dateFin);

    BondeLivraisonDto findById(Long id);

    ResponseEntity<InputStreamResource> generatePdf(List<Long> ids)
            throws DocumentException, IOException;

    List<Long> findAllIds(
            String refBondeLivraison,
            Double minMontatnTTC,
            Double maxMontatnTTC,
            Long idClient,
            LocalDate dateDebut,
            LocalDate dateFin);

    ResponseEntity<Void> deleteLingeBondeLivraison(Long bondeId, Long ligneBondeLivraisonId);

    ResponseEntity<Void> ajouterLingeDevis(
            Long devisId, Long idProduit, double prix, Integer quatite, Integer remise);

    Long convertToDevis(Long bondeId);

    Long convertToFacture(Long bondeId);


}
