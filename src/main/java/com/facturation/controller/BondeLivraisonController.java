package com.facturation.controller;

import com.facturation.controller.api.BondeLivraisonApi;
import com.facturation.dto.BondeLivraisonDto;
import com.facturation.model.BondeLivraison;
import com.facturation.repository.BondeLivraisonRepository;
import com.facturation.service.BondeLivraisonService;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
public class BondeLivraisonController implements BondeLivraisonApi {

    @Autowired
    private BondeLivraisonService bondeLivraisonService;

    @Override
    public BondeLivraisonDto save(BondeLivraison dto) {
        return bondeLivraisonService.save(dto);
    }

    @Override
    public Page<BondeLivraisonDto> findAll(int page, int size, String refBondeLivraison, Double minMontatnTTC, Double maxMontatnTTC, Long idClient, LocalDate dateDebut, LocalDate dateFin) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (refBondeLivraison != null && refBondeLivraison.equals("")) {
            refBondeLivraison = null;
        }
        return bondeLivraisonService.findAll(
                pageable,
                refBondeLivraison,
                minMontatnTTC,
                maxMontatnTTC,
                idClient,
                dateDebut,
                dateFin);
    }

    @Override
    public BondeLivraisonDto findById(Long id) {
        return bondeLivraisonService.findById(id);
    }

    @Override
    public ResponseEntity<InputStreamResource> generatePdf(List<Long> ids) throws DocumentException, IOException {
        return bondeLivraisonService.generatePdf(ids);
    }

    @Override
    public List<Long> findAllIds(String refBondeLivraison, Double minMontatnTTC, Double maxMontatnTTC, Long idClient, LocalDate dateDebut, LocalDate dateFin) {
        return bondeLivraisonService.findAllIds(
                refBondeLivraison, minMontatnTTC, maxMontatnTTC, idClient, dateDebut, dateFin);
    }

    @Override
    public ResponseEntity<Void> deleteLigneBonde(Long idBonde, Long idLigneBonde) {
        return bondeLivraisonService.deleteLingeBondeLivraison(idBonde, idLigneBonde);
    }

    @Override
    public ResponseEntity<Void> ajouterLigneBonde(Long bondeId, Long idProduit, Double prix, Integer quantite, Integer remise) {
        return bondeLivraisonService.ajouterLingeDevis(bondeId, idProduit, prix, quantite, remise);
    }

    @Override
    public ResponseEntity<Long> convertToDevis(Long id) {
        return ResponseEntity.ok().body(bondeLivraisonService.convertToDevis(id));
    }

    @Override
    public ResponseEntity<Long> convertToFacture(Long id) {
        return ResponseEntity.ok().body(bondeLivraisonService.convertToFacture(id));
    }

}
