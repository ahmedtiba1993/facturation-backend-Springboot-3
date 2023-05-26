package com.facturation.controller.api;

import com.facturation.dto.ClientDto;
import com.facturation.dto.FactureDto;
import com.facturation.dto.ProduitDto;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

import static com.facturation.utils.Constants.*;

public interface FactureApi {

    @PostMapping(value = FACTURE_ENDPOINT + "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    FactureDto save(@RequestBody FactureDto dto);

    @GetMapping(value = FACTURE_ENDPOINT + "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    Page<FactureDto> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size,
                             @RequestParam(required = false) String refFacture,
                             @RequestParam(required = false) Double minMontatnTTC ,
                             @RequestParam(required = false) Double maxMontatnTTC,
                             @RequestParam(required = false) Boolean paymentStatus,
                             @RequestParam(required = false) Long idClient,
                             @RequestParam(required = false) LocalDate dateDebut,
                             @RequestParam(required = false) LocalDate dateFin
                             );

    @GetMapping(value = FACTURE_ENDPOINT + "/{idFacture}", produces = MediaType.APPLICATION_JSON_VALUE)
    FactureDto findById(@PathVariable("idFacture") Long id);

    @GetMapping(value = FACTURE_ENDPOINT +"/generate-pdf"+ "/{idFacture}", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<InputStreamResource> generatePdf(@PathVariable("idFacture") Long id) throws DocumentException, IOException;

    @PostMapping(value = FACTURE_ENDPOINT +"/statutupdate/{idFacture}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateStatut(@PathVariable("idFacture") Long id);

    @GetMapping(value =STATISTIQUE_ENDPOINT+ "/statique" , produces = MediaType.APPLICATION_JSON_VALUE)
    com.facturation.model.projection.Statistique getStatistique();

}
