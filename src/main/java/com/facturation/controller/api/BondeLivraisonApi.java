package com.facturation.controller.api;

import com.facturation.dto.BondeLivraisonDto;
import com.facturation.model.BondeLivraison;
import com.facturation.model.projection.ClientRecapProjection;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static com.facturation.utils.Constants.BONDE_LIVRAISON_ENDPOINT;

public interface BondeLivraisonApi {

    @PostMapping(value = BONDE_LIVRAISON_ENDPOINT + "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    BondeLivraisonDto save(@RequestBody BondeLivraison dto);

    @GetMapping(value = BONDE_LIVRAISON_ENDPOINT + "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    Page<BondeLivraisonDto> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size, @RequestParam(required = false) String refBondeLivraison, @RequestParam(required = false) Double minMontatnTTC, @RequestParam(required = false) Double maxMontatnTTC, @RequestParam(required = false) Long idClient, @RequestParam(required = false) LocalDate dateDebut, @RequestParam(required = false) LocalDate dateFin);

    @GetMapping(value = BONDE_LIVRAISON_ENDPOINT + "/{idBondeLivraison}", produces = MediaType.APPLICATION_JSON_VALUE)
    BondeLivraisonDto findById(@PathVariable("idBondeLivraison") Long id);

    @GetMapping(value = BONDE_LIVRAISON_ENDPOINT + "/generate-pdf")
    ResponseEntity<InputStreamResource> generatePdf(@RequestParam List<Long> ids) throws DocumentException, IOException;

    @GetMapping(value = BONDE_LIVRAISON_ENDPOINT + "/allIds", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Long> findAllIds(@RequestParam(required = false) String refBondeLivraison, @RequestParam(required = false) Double minMontatnTTC, @RequestParam(required = false) Double maxMontatnTTC, @RequestParam(required = false) Long idClient, @RequestParam(required = false) LocalDate dateDebut, @RequestParam(required = false) LocalDate dateFin);

    @DeleteMapping(value = BONDE_LIVRAISON_ENDPOINT + "/deleteLigneBonde/{idBonde}/{idLigneBonde}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> deleteLigneBonde(@PathVariable("idBonde") Long idBonde, @PathVariable("idLigneBonde") Long idLigneBonde);

    @PostMapping(value = BONDE_LIVRAISON_ENDPOINT + "/ajouterLigneBonde/{idBonde}/{idProduit}/{prix}/{quantite}/{remise}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> ajouterLigneBonde(@PathVariable("idBonde") Long bondeId, @PathVariable("idProduit") Long idProduit, @PathVariable("prix") Double prix, @PathVariable("quantite") Integer quantite, @PathVariable("remise") Integer remise);

    @PostMapping(value = BONDE_LIVRAISON_ENDPOINT+ "/convert-to-devis/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Long> convertToDevis(@PathVariable("id") Long id);

    @PostMapping(value = BONDE_LIVRAISON_ENDPOINT+ "/convert-to-facture/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Long> convertToFacture(@PathVariable("id") Long id);
}
