package com.facturation.service.impl;

import com.facturation.dto.FactureDto;
import com.facturation.dto.LigneFactureDto;
import com.facturation.dto.ProduitDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.Client;
import com.facturation.model.Facture;
import com.facturation.model.LigneFacture;
import com.facturation.model.Produit;
import com.facturation.repository.ClientRepository;
import com.facturation.repository.FactureRepository;
import com.facturation.repository.LigneFactureRepository;
import com.facturation.repository.ProduitRepository;
import com.facturation.service.FactureService;
import com.facturation.service.TimbreFiscalService;
import com.facturation.validator.FactureValidator;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.facturation.utils.MontantEnLettres.convertirEnLettres;

@Service
@Slf4j
public class FactureServiceImpl implements FactureService {

    private FactureRepository factureRepository;
    private ClientRepository clientRepository;
    private ProduitRepository produitRepository;
    private LigneFactureRepository ligneFactureRepository;
    private TimbreFiscalService timbreFiscalService;

    @Autowired
    public FactureServiceImpl(FactureRepository factureRepository, ClientRepository clientRepository, ProduitRepository produitRepository, LigneFactureRepository ligneFactureRepository, TimbreFiscalService timbreFiscalService) {
        this.factureRepository = factureRepository;
        this.clientRepository = clientRepository;
        this.produitRepository = produitRepository;
        this.ligneFactureRepository = ligneFactureRepository;
        this.timbreFiscalService = timbreFiscalService;
    }

    @Override
    public FactureDto save(FactureDto dto) {
        List<String> errors = FactureValidator.validate(dto);

        if(!errors.isEmpty()) {
            log.error("Facture is not valid {} ",dto);
            throw new InvalidEntityException("Facture n est pas valide", ErrorCodes.FACTURE_NOT_VALID,errors);
        }

        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
        if(client.isEmpty()){
            log.error("Client not fond dans facture ",dto.getClient().getId());
            throw new EntityNotFoundException("Aucune client trouvée");
        }

        List<String> produitErrors= new ArrayList<String>();
        if(dto.getLignesFacture() != null){
            for (LigneFactureDto ligneFactureDto : dto.getLignesFacture()) {
                if(ligneFactureDto.getProduit() != null){
                    Optional<Produit> produit = produitRepository.findById(ligneFactureDto.getProduit().getId());
                    if(produit.isEmpty()) {
                        log.error("Produit not fond dans facture ",ligneFactureDto.getProduit().getId());
                        produitErrors.add("prodtui introvable '");
                    }
                }
            }
        }

        if(!produitErrors.isEmpty()) {
            log.warn("");
            throw new InvalidEntityException("prodiot n'existe pas dans la BDD",ErrorCodes.PRODUIT_NOT_FOUND,produitErrors);
        }

        int tauxTva = 19;
        dto.setTauxTVA(tauxTva);
        dto.setReference(generateReference(client.get().getCode()));
        dto.setTimbreFiscale(timbreFiscalService.getTimbreFiscale().getMontant());
        Facture saveFacture = factureRepository.save(FactureDto.toEntity(dto));

        double montantTotalProduit = 0.0;
        int remise = dto.getClient().getRemise();
        if(dto.getLignesFacture() != null) {
            for (LigneFactureDto ligneFact : dto.getLignesFacture()) {
                double montantProduit = ligneFact.getProduit().getPrix() * ligneFact.getQuantite();
                if(ligneFact.getProduit().getEtatRemise() == true){
                    montantTotalProduit = montantTotalProduit + (montantProduit - (montantProduit * (remise / 100.0)));
                }else{
                    montantTotalProduit = montantTotalProduit + montantProduit;
                }
                LigneFacture ligneFacture=LigneFactureDto.toEntity(ligneFact);
                ligneFacture.setFacture(saveFacture);
                ligneFacture.setPrixUnitaire(ligneFact.getProduit().getPrix());
                ligneFacture.setRemise(remise);
                ligneFacture.setPrixTotal((montantProduit - (montantProduit * (remise / 100.0))));
                ligneFactureRepository.save(ligneFacture);
            }
        }
        double montantTotal = montantTotalProduit + (montantTotalProduit * (tauxTva / 100.0));
        factureRepository.updateMontantTotal(saveFacture.getId(), montantTotalProduit,montantTotal);
        return FactureDto.fromEntity(saveFacture);
    }
    @Override
    public String generateReference(String codeClient){
        LocalDate today = LocalDate.now();
        int year = today.getYear();

        int numeroFactureParClient = factureRepository.countByYers(year)+1;
        String nombreDeFacturesFormatte = String.format("%04d", numeroFactureParClient);

        return year + "-" + codeClient + "-" +nombreDeFacturesFormatte;
    }

    @Override
    public Page<FactureDto> findAll(Pageable pageable) {
        Page<Facture> factures = factureRepository.findAll(pageable);
        Function<Facture, FactureDto> converter = FactureDto::fromEntity;
        Page<FactureDto> factureDtosPage = factures.map(converter);
        return factureDtosPage;
    }

    @Override
    public FactureDto findById(Long id) {
        if ( id == null) {
            return null;
        }

        Optional<Facture> facture = factureRepository.findById(id);
        FactureDto dto = facture.map(FactureDto::fromEntity).orElse(null);

        if (dto == null) {
            throw new EntityNotFoundException("Aucune facture trouvée dans la base de données",
                    ErrorCodes.FACTURE_NOT_FOUND);
        }

        return dto;
    }

    @Override
    public ResponseEntity<InputStreamResource> generatePdf(Long id) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);

        Optional<Facture> facutre = factureRepository.findById(id);

        document.open();

        Image img = Image.getInstance("src/main/resources/logofacture.png");
        img.scalePercent(30);
        img.setScaleToFitLineWhenOverflow(true);
        document.add(img);

        PdfPCell cellTitle = new PdfPCell();
        Paragraph title = new Paragraph("Alarme Assistace", new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        cellTitle.addElement(title);

        Paragraph subTitle = new Paragraph("Nifes Jilani", new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL));
        subTitle.setAlignment(Element.ALIGN_CENTER);
        cellTitle.addElement(subTitle);

        PdfPTable tableTile = new PdfPTable(1);
        tableTile.setWidthPercentage(100);
        cellTitle.setBorder(Rectangle.NO_BORDER);
        tableTile.addCell(cellTitle);

        document.add(tableTile);


        // création de la table avec deux colonnes
        PdfPTable tableHeader = new PdfPTable(2);
        tableHeader.setWidthPercentage(100);

        // première colonne : informations de la facture
        Paragraph p1 = new Paragraph("Alarme Assistance");
        p1.setLeading(0, 1.5f); // définit l'espacement entre les lignes à 1.5
        Paragraph p2 = new Paragraph("AV république 5000 Monastir");
        p2.setLeading(0, 1.5f);
        Paragraph p3 = new Paragraph("En face de station de bus");
        p3.setLeading(0, 1.5f);
        Paragraph p4 = new Paragraph("Matricule fiscal: 1418543 C/A/C/000");
        p4.setLeading(0, 1.5f);
        Paragraph p5 = new Paragraph("Registre de commerce : A15185872015");
        p5.setLeading(0, 1.5f);

        PdfPCell celltableHeader1 = new PdfPCell();
        celltableHeader1.addElement(p1);
        celltableHeader1.addElement(p2);
        celltableHeader1.addElement(p3);
        celltableHeader1.addElement(p4);
        celltableHeader1.addElement(p5);
        celltableHeader1.setBorder(Rectangle.NO_BORDER);
        tableHeader.addCell(celltableHeader1);


        // deuxième colonne : informations du client
        PdfPCell celltableHeader2 = new PdfPCell();
        Paragraph p6 = new Paragraph("Téléphone: 73 467 940");
        p6.setLeading(0, 1.5f);
        Paragraph p7 = new Paragraph("Fax: 73 467 940");
        p7.setLeading(0, 1.5f);
        Paragraph p8 = new Paragraph("Mobile: 97 366 747");
        p8.setLeading(0, 1.5f);
        celltableHeader2.addElement(p6);
        celltableHeader2.addElement(p7);
        celltableHeader2.addElement(p8);
        celltableHeader2.setBorder(Rectangle.NO_BORDER);
        celltableHeader2.setPaddingLeft(110);
        tableHeader.addCell(celltableHeader2);

        tableHeader.setSpacingAfter(10f);
        document.add(tableHeader);

        LineSeparator ls = new LineSeparator();
        ls.setOffset(0);
        document.add(new Chunk(ls));


        // création de la table avec deux colonnes
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // première colonne : informations de la facture
        PdfPCell celltable1 = new PdfPCell();
        Paragraph f1 = new Paragraph("Reference: "+facutre.get().getReference());
        f1.setLeading(0, 1.5f);

        Paragraph f2 = new Paragraph("Date: "+facutre.get().getDateFacture());
        f2.setLeading(0, 1.5f);

        celltable1.setBorder(Rectangle.NO_BORDER);
        celltable1.addElement(f1);
        celltable1.addElement(f2);
        table.addCell(celltable1);


        // deuxième colonne : informations du client
        PdfPCell celltable2 = new PdfPCell();
        Paragraph f3 = new Paragraph("Client: "+facutre.get().getClient().getNomCommercial());
        f3.setLeading(0, 1.5f);

        Paragraph f4 = new Paragraph("Adresse: "+facutre.get().getClient().getAdresse());
        f4.setLeading(0, 1.5f);

        Paragraph f5 = new Paragraph("MF: "+facutre.get().getClient().getCode());
        f5.setLeading(0, 1.5f);

        celltable2.setBorder(Rectangle.NO_BORDER);
        celltable2.addElement(f3);
        celltable2.addElement(f4);
        celltable2.addElement(f5);
        celltable2.setPaddingLeft(110);
        table.addCell(celltable2);

        table.setSpacingAfter(10f);
        document.add(table);


        // Création d'un tableau avec 6 colonnes
        float[] columnWidths = {150f, 300f, 80f , 150f , 80f , 150f};
        PdfPTable tableFacture = new PdfPTable(columnWidths);
        tableFacture.setWidthPercentage(110);

        PdfPCell cell11 = new PdfPCell(new Phrase("Code produit"));
        cell11.setHorizontalAlignment(Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
        cell11.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
        tableFacture.addCell(cell11);

        PdfPCell cell12 = new PdfPCell(new Phrase("Désignation"));
        cell12.setHorizontalAlignment(Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
        cell12.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
        tableFacture.addCell(cell12);

        PdfPCell cell13 = new PdfPCell(new Phrase("Quantité"));
        cell13.setHorizontalAlignment(Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
        cell13.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
        tableFacture.addCell(cell13);

        PdfPCell cell14 = new PdfPCell(new Phrase("Prix Unitaire"));
        cell14.setHorizontalAlignment(Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
        cell14.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
        tableFacture.addCell(cell14);

        PdfPCell cell15 = new PdfPCell(new Phrase("Remise"));
        cell15.setHorizontalAlignment(Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
        cell15.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
        tableFacture.addCell(cell15);

        PdfPCell cell16 = new PdfPCell(new Phrase("Total HT"));
        cell16.setHorizontalAlignment(Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
        cell16.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
        tableFacture.addCell(cell16);

        DecimalFormat df = new DecimalFormat("#0.000");
        // Ajout des produits
                for (LigneFacture l : facutre.get().getLignesFacture()) {

                    tableFacture.addCell(new PdfPCell(new Phrase(l.getProduit().getCode()))).setHorizontalAlignment(Element.ALIGN_CENTER);
                    tableFacture.addCell(new PdfPCell(new Phrase(l.getProduit().getDescription()))).setHorizontalAlignment(Element.ALIGN_CENTER);
                    tableFacture.addCell(new PdfPCell(new Phrase(String.valueOf(l.getQuantite())))).setHorizontalAlignment(Element.ALIGN_CENTER);
                    tableFacture.addCell(new PdfPCell(new Phrase(String.valueOf(df.format(l.getPrixUnitaire())+" TND")))).setHorizontalAlignment(Element.ALIGN_CENTER);
                    tableFacture.addCell(new PdfPCell(new Phrase(String.valueOf(l.getRemise())+"%"))).setHorizontalAlignment(Element.ALIGN_CENTER);
                    tableFacture.addCell(new PdfPCell(new Phrase(df.format(l.getPrixTotal())+" TND"))).setHorizontalAlignment(Element.ALIGN_CENTER);
                }

        // Ajout du tableau au document
        tableFacture.setSpacingAfter(20f);

        document.add(tableFacture);


        PdfPTable tablePrix = new PdfPTable(2);
        tablePrix.setWidthPercentage(50); // largeur de la table sur 50% de la page
        tablePrix.setHorizontalAlignment(Element.ALIGN_RIGHT); // alignement à droite

        PdfPCell cellTotalBrut = new PdfPCell(new Phrase("Total brut HT"));
        cellTotalBrut.setBorder(Rectangle.NO_BORDER); // supprime les bordures de la cellule
        cellTotalBrut.setPaddingTop(20);
        cellTotalBrut.setPaddingBottom(7);
        tablePrix.addCell(cellTotalBrut);

        PdfPCell cellTotalBrutValue = new PdfPCell(new Phrase(String.valueOf(df.format(facutre.get().getMontantHt())+ " TND")));
        cellTotalBrutValue.setBorder(Rectangle.NO_BORDER);
        cellTotalBrutValue.setPaddingTop(20);
        cellTotalBrutValue.setPaddingBottom(7);
        tablePrix.addCell(cellTotalBrutValue);

        PdfPCell cellTVA = new PdfPCell(new Phrase("TVA 19 %"));
        cellTVA.setBorder(Rectangle.NO_BORDER);
        cellTVA.setPaddingBottom(7);
        tablePrix.addCell(cellTVA);

        PdfPCell cellTVAValue = new PdfPCell(new Phrase(String.valueOf(df.format(facutre.get().getMontantHt()*0.19))+" TND"));
        cellTVAValue.setPaddingBottom(7);
        cellTVAValue.setBorder(Rectangle.NO_BORDER);
        tablePrix.addCell(cellTVAValue);

        PdfPCell cellDroitTimbre = new PdfPCell(new Phrase("Droit de timbre"));
        cellDroitTimbre.setPaddingBottom(7);
        cellDroitTimbre.setBorder(Rectangle.NO_BORDER);
        tablePrix.addCell(cellDroitTimbre);

        PdfPCell cellDroitTimbreValue = new PdfPCell(new Phrase("1.000 TND"));
        cellDroitTimbreValue.setPaddingBottom(7);
        cellDroitTimbreValue.setBorder(Rectangle.NO_BORDER);
        tablePrix.addCell(cellDroitTimbreValue);

        PdfPCell cellTotalTTC = new PdfPCell(new Phrase("Total TTC" , new Font(Font.FontFamily.HELVETICA, 15,Font.BOLD)));
        cellTotalTTC.setBorder(Rectangle.NO_BORDER);
        tablePrix.addCell(cellTotalTTC);

        PdfPCell cellTotalTTCValue = new PdfPCell(new Phrase(String.valueOf(df.format(facutre.get().getMontantTTC()))+ " TND", new Font(Font.FontFamily.HELVETICA, 15,Font.BOLD)));
        cellTotalTTCValue.setBorder(Rectangle.NO_BORDER);
        tablePrix.addCell(cellTotalTTCValue);

        document.add(tablePrix); // ajoute la table au document

        int partieEntiere = (int) Math.floor(facutre.get().getMontantTTC());
        double partieDecimale = facutre.get().getMontantTTC() - Math.floor(facutre.get().getMontantTTC());
        int resultat = (int) (partieDecimale * 1000);


        Paragraph pp1 = new Paragraph("Arrêté la présente facture à la somme de ");
        document.add(pp1);
        Paragraph pp2 = new Paragraph(convertirEnLettres(partieEntiere) +" dinars et "+convertirEnLettres(resultat)+" millimes");
        document.add(pp2);

        document.close();



        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename="+facutre.get().getReference()+".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(inputStream));
    }

    @Override
    public ResponseEntity<Void> updateStatus(Long id) {

        if ( id == null) {
            return null;
        }

        Optional<Facture> facture = factureRepository.findById(id);

        if (!facture.isPresent()) {
            throw new EntityNotFoundException("Aucune facture trouvée dans la base de données",
                    ErrorCodes.FACTURE_NOT_FOUND);
        }
        if(facture.get().getPaymentStatus() != null && facture.get().getPaymentStatus()){
            factureRepository.setStatusFalse(id);
        }else{
            factureRepository.setStatusTrue(id);
        }

        return ResponseEntity.ok().build();
    }

}
