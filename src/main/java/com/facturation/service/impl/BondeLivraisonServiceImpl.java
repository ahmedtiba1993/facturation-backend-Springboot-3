package com.facturation.service.impl;

import com.facturation.dto.BondeLivraisonDto;
import com.facturation.dto.DevisDto;
import com.facturation.dto.LigneBondeLivraisonDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.*;
import com.facturation.repository.*;
import com.facturation.service.BondeLivraisonService;
import com.facturation.service.TimbreFiscalService;
import com.facturation.validator.BondeLivraisonValidator;
import com.facturation.validator.DevisValidator;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.facturation.utils.MontantEnLettres.convertirEnLettres;

@Service
@RequiredArgsConstructor
@Slf4j
public class BondeLivraisonServiceImpl implements BondeLivraisonService {

    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final TvaRepository tvaRepository;
    private final TimbreFiscalService timbreFiscalService;
    private final NumFactureRepository numFactureRepository;
    private final BondeLivraisonRepository bondeLivraisonRepository;
    private final LigneBondeLivraisonRepository ligneBondeLivraisonRepository;
    private final DevisRepository devisRepository;
    private final LigneDevisRepository ligneDevisRepository;
    private final FactureRepository factureRepository;
    private final LigneFactureRepository ligneFactureRepository;

    @Override
    public BondeLivraisonDto save(BondeLivraison bondeLivraison) {
        List<String> errors = BondeLivraisonValidator.validate(bondeLivraison);

        if (!errors.isEmpty()) {
            throw new InvalidEntityException(
                    "Bonde Livraison n est pas valide", ErrorCodes.FACTURE_NOT_VALID, errors);
        }

        Optional<Client> client = clientRepository.findById(bondeLivraison.getClient().getId());
        if (client.isEmpty()) {
            throw new EntityNotFoundException("Aucune client trouvée");
        }

        List<String> produitErrors = new ArrayList<String>();
        if (bondeLivraison.getLigneBondeLivraisons() != null) {
            for (LigneBondeLivraison ligneBondeLivraison : bondeLivraison.getLigneBondeLivraisons()) {
                if (ligneBondeLivraison.getProduit() != null) {
                    Optional<Produit> produit = produitRepository.findById(ligneBondeLivraison.getProduit().getId());
                    if (produit.isEmpty()) {
                        produitErrors.add("prodtui introvable '");
                    }
                }
            }
        }

        if (!produitErrors.isEmpty()) {
            log.warn("");
            throw new InvalidEntityException(
                    "prodiot n'existe pas dans la BDD", ErrorCodes.PRODUIT_NOT_FOUND, produitErrors);
        }

        int tauxTva = tvaRepository.getTvaByCode("TVA").getTva();
        double timbre = timbreFiscalService.getTimbreFiscale().getMontant();
        bondeLivraison.setTauxTVA(tauxTva);
        bondeLivraison.setReference(generateReference());
        bondeLivraison.setTimbreFiscale(timbre);
        BondeLivraison saveBondeLivraison = bondeLivraisonRepository.save(bondeLivraison);

        double montantTotalProduit = 0.0;
        if (bondeLivraison.getLigneBondeLivraisons() != null) {
            for (LigneBondeLivraison ligneBondeLivraison : bondeLivraison.getLigneBondeLivraisons()) {
                int remise = ligneBondeLivraison.getRemise();
                double montantProduit = ligneBondeLivraison.getProduit().getPrix() * ligneBondeLivraison.getQuantite();
                if (ligneBondeLivraison.getProduit().getEtatRemise() == true) {
                    montantTotalProduit =
                            montantTotalProduit + (montantProduit - (montantProduit * (remise / 100.0)));
                } else {
                    montantTotalProduit = montantTotalProduit + montantProduit;
                }
                ligneBondeLivraison.setBondeLivraison(saveBondeLivraison);
                ligneBondeLivraison.setPrixUnitaire(ligneBondeLivraison.getProduit().getPrix());
                ligneBondeLivraison.setRemise(remise);
                ligneBondeLivraison.setPrixTotal((montantProduit - (montantProduit * (remise / 100.0))));
                ligneBondeLivraisonRepository.save(ligneBondeLivraison);
            }
        }
        double montantTotal =
                montantTotalProduit + (montantTotalProduit * (tauxTva / 100.0)) + (timbre / 1000);
        bondeLivraisonRepository.updateMontantTotal(saveBondeLivraison.getId(), montantTotalProduit, montantTotal);
        return BondeLivraisonDto.fromEntity(saveBondeLivraison);
    }

    @Override
    public Page<BondeLivraisonDto> findAll(Pageable pageable, String refBondeLivraison, Double minMontatnTTC, Double maxMontatnTTC, Long idClient, LocalDate dateDebut, LocalDate dateFin) {
        Page<BondeLivraison> bondeLivraisons =
                bondeLivraisonRepository.findAllFiltre(
                        pageable,
                        refBondeLivraison,
                        minMontatnTTC,
                        maxMontatnTTC,
                        idClient,
                        dateDebut,
                        dateFin);
        Function<BondeLivraison, BondeLivraisonDto> converter = BondeLivraisonDto::fromEntity;
        Page<BondeLivraisonDto> bondeLivraisonDtoPage = bondeLivraisons.map(converter);
        return bondeLivraisonDtoPage;
    }

    @Override
    public BondeLivraisonDto findById(Long id) {
        if (id == null) {
            return null;
        }

        Optional<BondeLivraison> bondeLivraison = bondeLivraisonRepository.findById(id);
        BondeLivraisonDto dto = bondeLivraison.map(BondeLivraisonDto::fromEntity).orElse(null);

        if (dto == null) {
            throw new EntityNotFoundException(
                    "Aucune facture trouvée dans la base de données", ErrorCodes.FACTURE_NOT_FOUND);
        }

        return dto;
    }

    @Override
    public ResponseEntity<InputStreamResource> generatePdf(List<Long> ids) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);

        document.open();
        List<BondeLivraison> BondeLivraisonList = bondeLivraisonRepository.findBondeLivraisonToPdf(ids);

        for (BondeLivraison bondeLivraison : BondeLivraisonList) {
            document.newPage();

            Image img = Image.getInstance("classpath:logofacture.png");
            img.scalePercent(35);
            img.setScaleToFitLineWhenOverflow(true);
            // Positionner l'image à gauche de la page
            float marginLeft = document.leftMargin(); // Récupérer la marge gauche du document
            float imageX = marginLeft + 20; // Décalage de 20 unités de la marge gauche
            float imageY =
                    document.getPageSize().getHeight()
                            - img.getScaledHeight(); // Position verticale en haut de la page
            img.setAbsolutePosition(imageX, imageY);
            document.add(img);

            PdfPCell cellTitle = new PdfPCell();
            Paragraph title =
                    new Paragraph("Alarme Assistace", new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            cellTitle.addElement(title);

            Paragraph subTitle =
                    new Paragraph("Nifes Jilani", new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL));
            subTitle.setAlignment(Element.ALIGN_CENTER);
            cellTitle.addElement(subTitle);

            Paragraph sousSubTitle =
                    new Paragraph(
                            "Vente et installation de matériel de sécurité électronique",
                            new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL));
            sousSubTitle.setAlignment(Element.ALIGN_CENTER);
            cellTitle.addElement(sousSubTitle);

            PdfPTable tableTile = new PdfPTable(1);
            tableTile.setWidthPercentage(100);
            cellTitle.setBorder(Rectangle.NO_BORDER);
            tableTile.addCell(cellTitle);

            tableTile.setSpacingAfter(20);
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
            Paragraph f1 = new Paragraph("Bonde de livraison : " + bondeLivraison.getReference());
            f1.setLeading(0, 1.5f);

            Paragraph f2 = new Paragraph("Date: " + bondeLivraison.getDateBondeLivraison());
            f2.setLeading(0, 1.5f);

            celltable1.setBorder(Rectangle.NO_BORDER);
            celltable1.addElement(f1);
            celltable1.addElement(f2);
            table.addCell(celltable1);

            // deuxième colonne : informations du client
            PdfPCell celltable2 = new PdfPCell();
            Paragraph f3 = new Paragraph("Client: " + bondeLivraison.getClient().getNomCommercial());
            f3.setLeading(0, 1.5f);

            Paragraph f4 = new Paragraph("Adresse: " + bondeLivraison.getClient().getAdresse());
            f4.setLeading(0, 1.5f);

            Paragraph f5 = new Paragraph("MF: " + bondeLivraison.getClient().getCode());
            f5.setLeading(0, 1.5f);

            celltable2.setBorder(Rectangle.NO_BORDER);
            celltable2.addElement(f3);
            celltable2.addElement(f4);
            celltable2.addElement(f5);
            celltable2.setPaddingLeft(30);
            table.addCell(celltable2);

            table.setSpacingAfter(10f);
            document.add(table);

            // Création d'un tableau avec 6 colonnes
            float[] columnWidths = {150f, 300f, 80f, 150f, 80f, 150f};
            PdfPTable tableFacture = new PdfPTable(columnWidths);
            tableFacture.setWidthPercentage(110);

            PdfPCell cell11 = new PdfPCell(new Phrase("Code produit"));
            cell11.setHorizontalAlignment(
                    Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
            cell11.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
            tableFacture.addCell(cell11);

            PdfPCell cell12 = new PdfPCell(new Phrase("Désignation"));
            cell12.setHorizontalAlignment(
                    Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
            cell12.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
            tableFacture.addCell(cell12);

            PdfPCell cell13 = new PdfPCell(new Phrase("Quantité"));
            cell13.setHorizontalAlignment(
                    Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
            cell13.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
            tableFacture.addCell(cell13);

            PdfPCell cell14 = new PdfPCell(new Phrase("Prix Unitaire"));
            cell14.setHorizontalAlignment(
                    Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
            cell14.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
            tableFacture.addCell(cell14);

            PdfPCell cell15 = new PdfPCell(new Phrase("Remise"));
            cell15.setHorizontalAlignment(
                    Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
            cell15.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
            tableFacture.addCell(cell15);

            PdfPCell cell16 = new PdfPCell(new Phrase("Total HT"));
            cell16.setHorizontalAlignment(
                    Element.ALIGN_CENTER); // définit l'alignement horizontal au centre
            cell16.setBackgroundColor(BaseColor.LIGHT_GRAY); // définit la couleur de fond
            tableFacture.addCell(cell16);

            DecimalFormat df = new DecimalFormat("#0.000");
            // Ajout des produits
            for (LigneBondeLivraison l : bondeLivraison.getLigneBondeLivraisons()) {

                tableFacture
                        .addCell(new PdfPCell(new Phrase(l.getProduit().getCode())))
                        .setHorizontalAlignment(Element.ALIGN_CENTER);
                tableFacture
                        .addCell(new PdfPCell(new Phrase(l.getProduit().getDescription())))
                        .setHorizontalAlignment(Element.ALIGN_CENTER);
                tableFacture
                        .addCell(new PdfPCell(new Phrase(String.valueOf(l.getQuantite()))))
                        .setHorizontalAlignment(Element.ALIGN_CENTER);
                tableFacture
                        .addCell(
                                new PdfPCell(new Phrase(String.valueOf(df.format(l.getPrixUnitaire()) + " TND"))))
                        .setHorizontalAlignment(Element.ALIGN_CENTER);
                tableFacture
                        .addCell(new PdfPCell(new Phrase(String.valueOf(l.getRemise()) + "%")))
                        .setHorizontalAlignment(Element.ALIGN_CENTER);
                tableFacture
                        .addCell(new PdfPCell(new Phrase(df.format(l.getPrixTotal()) + " TND")))
                        .setHorizontalAlignment(Element.ALIGN_CENTER);
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

            PdfPCell cellTotalBrutValue =
                    new PdfPCell(new Phrase(String.valueOf(df.format(bondeLivraison.getMontantHt()) + " TND")));
            cellTotalBrutValue.setBorder(Rectangle.NO_BORDER);
            cellTotalBrutValue.setPaddingTop(20);
            cellTotalBrutValue.setPaddingBottom(7);
            tablePrix.addCell(cellTotalBrutValue);

            PdfPCell cellTVA = new PdfPCell(new Phrase("TVA 19 %"));
            cellTVA.setBorder(Rectangle.NO_BORDER);
            cellTVA.setPaddingBottom(7);
            tablePrix.addCell(cellTVA);

            PdfPCell cellTVAValue =
                    new PdfPCell(new Phrase(String.valueOf(df.format(bondeLivraison.getMontantHt() * 0.19)) + " TND"));
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

            PdfPCell cellTotalTTC =
                    new PdfPCell(new Phrase("Total TTC", new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD)));
            cellTotalTTC.setBorder(Rectangle.NO_BORDER);
            tablePrix.addCell(cellTotalTTC);

            PdfPCell cellTotalTTCValue =
                    new PdfPCell(
                            new Phrase(
                                    String.valueOf(df.format(bondeLivraison.getMontantTTC())) + " TND",
                                    new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD)));
            cellTotalTTCValue.setBorder(Rectangle.NO_BORDER);
            tablePrix.addCell(cellTotalTTCValue);
            tableFacture.setSpacingAfter(30);
            document.add(tablePrix); // ajoute la table au document

            int partieEntiere = (int) Math.floor(bondeLivraison.getMontantTTC());
            double partieDecimale = bondeLivraison.getMontantTTC() - Math.floor(bondeLivraison.getMontantTTC());
            int resultat = (int) (partieDecimale * 1000);

            Paragraph pp1 = new Paragraph("Arrêté la présente facture à la somme de :");
            document.add(pp1);
            Paragraph pp2 =
                    new Paragraph(
                            convertirEnLettres(partieEntiere) + " dinars et ( " + resultat + " ) millimes.");
            document.add(pp2);
        }
        document.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=test.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(inputStream));
    }

    @Override
    public List<Long> findAllIds(String refBondeLivraison, Double minMontatnTTC, Double maxMontatnTTC, Long idClient, LocalDate dateDebut, LocalDate dateFin) {
        return bondeLivraisonRepository.findAllIds(
                refBondeLivraison, minMontatnTTC, maxMontatnTTC, idClient, dateDebut, dateFin);
    }

    @Override
    public ResponseEntity<Void> deleteLingeBondeLivraison(Long bondeId, Long ligneBondeLivraisonId) {
        BondeLivraisonDto bondeLivraisonDto = findById(bondeId);
        LigneBondeLivraison ligneBondeLivraison = ligneBondeLivraisonRepository.findById(ligneBondeLivraisonId).get();

        double timbre = timbreFiscalService.getTimbreFiscale().getMontant();

        Double montantHt = bondeLivraisonDto.getMontantHt();
        Double montantTTC = bondeLivraisonDto.getMontantTTC();

        montantHt -= ligneBondeLivraison.getPrixTotal();
        montantTTC = montantHt + (montantHt * 0.19) + (timbre / 1000);

        bondeLivraisonRepository.updateMontant(bondeId, montantHt, montantTTC);

        this.ligneBondeLivraisonRepository.deleteById(ligneBondeLivraisonId);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> ajouterLingeDevis(Long devisId, Long idProduit, double prix, Integer quatite, Integer remise) {
        BondeLivraisonDto bondeLivraisonDto = findById(devisId);
        Produit produit = produitRepository.findById(idProduit).get();

        LigneBondeLivraison ligneBondeLivraison = new LigneBondeLivraison();
        ligneBondeLivraison.setProduit(produit);
        ligneBondeLivraison.setRemise(remise);
        ligneBondeLivraison.setPrixUnitaire(prix * quatite);
        ligneBondeLivraison.setQuantite(quatite);
        ligneBondeLivraison.setPrixTotal((quatite * prix) - ((quatite * prix) * (remise / 100.0)));

        BondeLivraison d = new BondeLivraison();
        d.setId(bondeLivraisonDto.getId());
        ligneBondeLivraison.setBondeLivraison(d);
        ligneBondeLivraisonRepository.save(ligneBondeLivraison);

        Double prixProduit = (quatite * prix) - ((quatite * prix) * (remise / 100.0));
        double timbre = timbreFiscalService.getTimbreFiscale().getMontant();

        Double montantHt = bondeLivraisonDto.getMontantHt() + prixProduit;
        Double montantTTC = montantHt + (montantHt * 0.19) + (timbre / 1000);

        bondeLivraisonRepository.updateMontant(devisId, montantHt, montantTTC);

        return ResponseEntity.ok().build();
    }

    @Override
    public Long convertToDevis(Long bondeId) {
        BondeLivraison bondeLivraison = bondeLivraisonRepository.findById(bondeId).orElseThrow(()-> new EntityNotFoundException("NOT_FOUND"));

        Devis devis = new Devis();
        devis.setReference(generateReferenceDevis());
        devis.setTauxTVA(bondeLivraison.getTauxTVA());
        devis.setDateDevis(bondeLivraison.getDateBondeLivraison());
        devis.setTimbreFiscale(bondeLivraison.getTimbreFiscale());
        devis.setClient(bondeLivraison.getClient());
        devis.setMontantHt(bondeLivraison.getMontantHt());
        devis.setMontantTTC(bondeLivraison.getMontantTTC());
        devis.setPaymentStatus(false);
        devis = devisRepository.save(devis);

        List<LigneDevis> ligneDevis = new ArrayList<>();
        Devis finalDevis = devis;
        bondeLivraison.getLigneBondeLivraisons().forEach(l -> {
            LigneDevis ldevis = new LigneDevis();
            ldevis.setProduit(l.getProduit());
            ldevis.setRemise(l.getRemise());
            ldevis.setQuantite(l.getQuantite());
            ldevis.setPrixUnitaire(l.getPrixUnitaire());
            ldevis.setPrixTotal(l.getPrixTotal());
            ldevis.setDevis(finalDevis);
            ligneDevisRepository.save(ldevis);
        });

        return devis.getId();
    }

    @Override
    public Long convertToFacture(Long bondeId) {
        BondeLivraison bondeLivraison = bondeLivraisonRepository.findById(bondeId).orElseThrow(()-> new EntityNotFoundException("NOT_FOUND"));
        // Copie des informations générales
        Facture facture = new Facture();
        facture.setDateFacture(bondeLivraison.getDateBondeLivraison());
        facture.setTauxTVA(bondeLivraison.getTauxTVA());
        facture.setMontantHt(bondeLivraison.getMontantHt());
        facture.setMontantTTC(bondeLivraison.getMontantTTC());
        facture.setTimbreFiscale(bondeLivraison.getTimbreFiscale());
        facture.setPaymentStatus(false);
        facture.setClient(bondeLivraison.getClient());
        facture.setReference(generateReferenceFacture());
        factureRepository.save(facture);

        List<LigneFacture> ligneFacturesList = new ArrayList<>();
        for (LigneBondeLivraison ligneBondeLivraison : bondeLivraison.getLigneBondeLivraisons()) {
            LigneFacture ligneFacture = new LigneFacture();
            ligneFacture.setProduit(ligneBondeLivraison.getProduit());
            ligneFacture.setQuantite(ligneBondeLivraison.getQuantite());
            ligneFacture.setPrixUnitaire(ligneBondeLivraison.getPrixUnitaire());
            ligneFacture.setPrixTotal(ligneBondeLivraison.getPrixTotal());
            ligneFacture.setRemise(ligneBondeLivraison.getRemise());
            ligneFacture.setFacture(facture);
            ligneFactureRepository.save(ligneFacture);
        }
        return facture.getId();
    }

    public String generateReferenceDevis() {

        LocalDate today = LocalDate.now();
        int year = today.getYear();

        DecimalFormat decimalFormat = new DecimalFormat("000");
        Integer numDevis = numFactureRepository.getNumDevis();
        String nombreDeDevisFormatte = decimalFormat.format(numDevis + 1);
        numFactureRepository.updateNumDevis(numDevis + 1);
        return nombreDeDevisFormatte + "-" + year;
    }

    public String generateReference() {

        LocalDate today = LocalDate.now();
        int year = today.getYear();

        DecimalFormat decimalFormat = new DecimalFormat("000");
        Integer numDevis = numFactureRepository.getNumDevis();
        String nombreDeDevisFormatte = decimalFormat.format(numDevis + 1);
        numFactureRepository.updateNumDevis(numDevis + 1);
        return nombreDeDevisFormatte + "-" + year;
    }

    public String generateReferenceFacture() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();

        DecimalFormat decimalFormat = new DecimalFormat("000");
        Integer numFacture = numFactureRepository.getNumFacture();
        String nombreDeFacturesFormatte = decimalFormat.format(numFacture + 1);
        numFactureRepository.updateNumFacture(numFacture + 1);
        return nombreDeFacturesFormatte + "-" + year;
    }
}
