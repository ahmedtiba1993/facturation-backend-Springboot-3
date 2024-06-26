package com.facturation.service.impl;

import com.facturation.dto.FactureDto;
import com.facturation.dto.LigneFactureDto;
import com.facturation.dto.UserDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.*;
import com.facturation.model.projection.RecapClient;
import com.facturation.model.projection.Statistique;
import com.facturation.repository.*;
import com.facturation.service.FactureService;
import com.facturation.service.TimbreFiscalService;
import com.facturation.user.User;
import com.facturation.user.UserRepository;
import com.facturation.validator.FactureValidator;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.facturation.utils.MontantEnLettres.convertirEnLettres;

@Service
@Slf4j
public class FactureServiceImpl implements FactureService {

    @Autowired
    private FactureRepository factureRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private LigneFactureRepository ligneFactureRepository;

    @Autowired
    private TimbreFiscalService timbreFiscalService;

    @Autowired
    private NumFactureRepository numFactureRepository;

    @Autowired
    private TvaRepository tvaRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DevisRepository devisRepository;
    @Autowired
    private LigneDevisRepository ligneDevisRepository;
    @Autowired
    private BondeLivraisonRepository bondeLivraisonRepository;
    @Autowired
    private LigneBondeLivraisonRepository ligneBondeLivraisonRepository;

    @Override
    public FactureDto save(FactureDto dto) {
        List<String> errors = FactureValidator.validate(dto);

        if (!errors.isEmpty()) {
            log.error("Facture is not valid {} ", dto);
            throw new InvalidEntityException("Facture n est pas valide", ErrorCodes.FACTURE_NOT_VALID, errors);
        }

        Optional<Client> client = clientRepository.findById(dto.getClient().getId());
        if (client.isEmpty()) {
            log.error("Client not fond dans facture ", dto.getClient().getId());
            throw new EntityNotFoundException("Aucune client trouvée");
        }

        List<String> produitErrors = new ArrayList<String>();
        if (dto.getLignesFacture() != null) {
            for (LigneFactureDto ligneFactureDto : dto.getLignesFacture()) {
                if (ligneFactureDto.getProduit() != null) {
                    Optional<Produit> produit = produitRepository.findById(ligneFactureDto.getProduit().getId());
                    if (produit.isEmpty()) {
                        log.error("Produit not found dans facture ", ligneFactureDto.getProduit().getId());
                        produitErrors.add("prodtui introvable '");
                    }
                }
            }
        }

        if (!produitErrors.isEmpty()) {
            log.warn("");
            throw new InvalidEntityException("prodiot n'existe pas dans la BDD", ErrorCodes.PRODUIT_NOT_FOUND, produitErrors);
        }

        int tauxTva = tvaRepository.getTvaByCode("TVA").getTva();
        double timbre = timbreFiscalService.getTimbreFiscale().getMontant();
        dto.setTauxTVA(tauxTva);
        dto.setReference(generateReference(client.get().getCode()));
        dto.setTimbreFiscale(timbre);
        Facture saveFacture = factureRepository.save(FactureDto.toEntity(dto));

        double montantTotalProduit = 0.0;
        if (dto.getLignesFacture() != null) {
            for (LigneFactureDto ligneFact : dto.getLignesFacture()) {
                int remise = ligneFact.getRemise();
                double montantProduit = ligneFact.getProduit().getPrix() * ligneFact.getQuantite();
                if (ligneFact.getProduit().getEtatRemise() == true) {
                    montantTotalProduit = montantTotalProduit + (montantProduit - (montantProduit * (remise / 100.0)));
                } else {
                    montantTotalProduit = montantTotalProduit + montantProduit;
                }
                LigneFacture ligneFacture = LigneFactureDto.toEntity(ligneFact);
                ligneFacture.setFacture(saveFacture);
                ligneFacture.setPrixUnitaire(ligneFact.getProduit().getPrix());
                ligneFacture.setRemise(remise);
                ligneFacture.setPrixTotal((montantProduit - (montantProduit * (remise / 100.0))));
                ligneFactureRepository.save(ligneFacture);
            }
        }
        double montantTotal = montantTotalProduit + (montantTotalProduit * (tauxTva / 100.0)) + (timbre / 1000);
        factureRepository.updateMontantTotal(saveFacture.getId(), montantTotalProduit, montantTotal);
        return FactureDto.fromEntity(saveFacture);
    }

    @Override
    public String generateReference(String codeClient) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();

        DecimalFormat decimalFormat = new DecimalFormat("000");
        Integer numFacture = numFactureRepository.getNumFacture();
        String nombreDeFacturesFormatte = decimalFormat.format(numFacture + 1);
        numFactureRepository.updateNumFacture(numFacture + 1);
        return nombreDeFacturesFormatte + "-" + year;
    }

    @Override
    public Page<FactureDto> findAll(Pageable pageable, String refFacture, Double minMontatnTTC, Double maxMontatnTTC, Boolean paymentStatus, Long idClient, LocalDate dateDebut, LocalDate dateFin) {
        Page<Facture> factures = factureRepository.findAllFiltre(pageable, refFacture, minMontatnTTC, maxMontatnTTC, paymentStatus, idClient, dateDebut, dateFin);
        Function<Facture, FactureDto> converter = FactureDto::fromEntity;
        Page<FactureDto> factureDtosPage = factures.map(converter);
        return factureDtosPage;
    }

    @Override
    public FactureDto findById(Long id) {
        if (id == null) {
            return null;
        }

        Optional<Facture> facture = factureRepository.findById(id);
        FactureDto dto = facture.map(FactureDto::fromEntity).orElse(null);

        if (dto == null) {
            throw new EntityNotFoundException("Aucune facture trouvée dans la base de données", ErrorCodes.FACTURE_NOT_FOUND);
        }

        return dto;
    }

    @Override
    @Transactional
    public ResponseEntity<InputStreamResource> generatePdf(List<Long> ids) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        User user = userRepository.findByEmail("admin").get();
        /*Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = new User();
        if (authentication != null && authentication.isAuthenticated()) {
            // Vous pouvez maintenant accéder aux informations de l'utilisateur
            //user = (User) authentication.getPrincipal();
        }*/

        document.open();
        List<Facture> factureList = factureRepository.findFactureToPdf(ids);

        for (Facture facutre : factureList) {
            document.newPage();

            Image img = Image.getInstance("classpath:logofacture.png");
            img.scalePercent(35);
            img.setScaleToFitLineWhenOverflow(true);
            // Positionner l'image à gauche de la page
            float marginLeft = document.leftMargin(); // Récupérer la marge gauche du document
            float imageX = marginLeft + 20; // Décalage de 20 unités de la marge gauche
            float imageY = document.getPageSize().getHeight() - img.getScaledHeight(); // Position verticale en haut de la page
            img.setAbsolutePosition(imageX, imageY);
            document.add(img);

            PdfPCell cellTitle = new PdfPCell();
            Paragraph title = new Paragraph("Alarme Assistance", new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            cellTitle.addElement(title);

            Paragraph subTitle = new Paragraph("Nifes Jilani", new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL));
            subTitle.setAlignment(Element.ALIGN_CENTER);
            cellTitle.addElement(subTitle);

            Paragraph sousSubTitle = new Paragraph("Vente et installation de matériel de sécurité électronique", new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL));
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

            String millions = user.getTel().toString().substring(0, 1);
            String centMille = user.getTel().toString().substring(1, 4);
            String mille = user.getTel().toString().substring(4);

            // deuxième colonne : informations du client
            PdfPCell celltableHeader2 = new PdfPCell();
            Paragraph p6 = new Paragraph("Téléphone: " + user.getTel().toString().substring(0, 2) + " " + user.getTel().toString().substring(2, 4) + " " + user.getTel().toString().substring(4));
            p6.setLeading(0, 1.5f);
            Paragraph p7 = new Paragraph("Fax: " + user.getFax().toString().substring(0, 2) + " " + user.getFax().toString().substring(2, 4) + " " + user.getFax().toString().substring(4));
            p7.setLeading(0, 1.5f);
            Paragraph p8 = new Paragraph("Mobile: " + user.getMobile().toString().substring(0, 2) + " " + user.getMobile().toString().substring(2, 4) + " " + user.getMobile().toString().substring(4));
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
            Paragraph f1 = new Paragraph("facture: " + facutre.getReference());
            f1.setLeading(0, 1.5f);

            Paragraph f2 = new Paragraph("Date: " + facutre.getDateFacture());
            f2.setLeading(0, 1.5f);

            celltable1.setBorder(Rectangle.NO_BORDER);
            celltable1.addElement(f1);
            celltable1.addElement(f2);
            table.addCell(celltable1);

            // deuxième colonne : informations du client
            PdfPCell celltable2 = new PdfPCell();
            Paragraph f3 = new Paragraph("Client: " + facutre.getClient().getNomCommercial());
            f3.setLeading(0, 1.5f);

            Paragraph f4 = new Paragraph("Adresse: " + facutre.getClient().getAdresse());
            f4.setLeading(0, 1.5f);

            Paragraph f5 = new Paragraph("MF: " + facutre.getClient().getCode());
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
            for (LigneFacture l : facutre.getLignesFacture()) {

                tableFacture.addCell(new PdfPCell(new Phrase(l.getProduit().getCode()))).setHorizontalAlignment(Element.ALIGN_CENTER);
                tableFacture.addCell(new PdfPCell(new Phrase(l.getProduit().getDescription()))).setHorizontalAlignment(Element.ALIGN_CENTER);
                tableFacture.addCell(new PdfPCell(new Phrase(String.valueOf(l.getQuantite())))).setHorizontalAlignment(Element.ALIGN_CENTER);
                tableFacture.addCell(new PdfPCell(new Phrase(String.valueOf(df.format(l.getPrixUnitaire()) + " TND")))).setHorizontalAlignment(Element.ALIGN_CENTER);
                tableFacture.addCell(new PdfPCell(new Phrase(String.valueOf(l.getRemise()) + "%"))).setHorizontalAlignment(Element.ALIGN_CENTER);
                tableFacture.addCell(new PdfPCell(new Phrase(df.format(l.getPrixTotal()) + " TND"))).setHorizontalAlignment(Element.ALIGN_CENTER);
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

            PdfPCell cellTotalBrutValue = new PdfPCell(new Phrase(String.valueOf(df.format(facutre.getMontantHt()) + " TND")));
            cellTotalBrutValue.setBorder(Rectangle.NO_BORDER);
            cellTotalBrutValue.setPaddingTop(20);
            cellTotalBrutValue.setPaddingBottom(7);
            tablePrix.addCell(cellTotalBrutValue);

            PdfPCell cellTVA = new PdfPCell(new Phrase("TVA 19 %"));
            cellTVA.setBorder(Rectangle.NO_BORDER);
            cellTVA.setPaddingBottom(7);
            tablePrix.addCell(cellTVA);

            PdfPCell cellTVAValue = new PdfPCell(new Phrase(String.valueOf(df.format(facutre.getMontantHt() * 0.19)) + " TND"));
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

            PdfPCell cellTotalTTC = new PdfPCell(new Phrase("Total TTC", new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD)));
            cellTotalTTC.setBorder(Rectangle.NO_BORDER);
            tablePrix.addCell(cellTotalTTC);

            PdfPCell cellTotalTTCValue = new PdfPCell(new Phrase(String.valueOf(df.format(facutre.getMontantTTC())) + " TND", new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD)));
            cellTotalTTCValue.setBorder(Rectangle.NO_BORDER);
            tablePrix.addCell(cellTotalTTCValue);
            tableFacture.setSpacingAfter(30);
            document.add(tablePrix); // ajoute la table au document

            int partieEntiere = (int) Math.floor(facutre.getMontantTTC());
            double partieDecimale = facutre.getMontantTTC() - Math.floor(facutre.getMontantTTC());
            int resultat = (int) Math.round(partieDecimale * 1000 + 0.0001);// Arrondi avec une petite quantité supplémentaire

            Paragraph pp1 = new Paragraph("Arrêté la présente facture à la somme de :");
            document.add(pp1);
            Paragraph pp2 = new Paragraph(convertirEnLettres(partieEntiere) + " dinars et ( " + resultat + " ) millimes.");
            document.add(pp2);
        }
        document.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=" + LocalDateTime.now() + ".pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(inputStream));
    }

    @Override
    public ResponseEntity<Void> updateStatus(Long id) {

        if (id == null) {
            return null;
        }

        Optional<Facture> facture = factureRepository.findById(id);

        if (!facture.isPresent()) {
            throw new EntityNotFoundException("Aucune facture trouvée dans la base de données", ErrorCodes.FACTURE_NOT_FOUND);
        }
        if (facture.get().getPaymentStatus() != null && facture.get().getPaymentStatus()) {
            factureRepository.setStatusFalse(id);
        } else {
            factureRepository.setStatusTrue(id);
        }

        return ResponseEntity.ok().build();
    }

    @Override
    public Statistique getStatistique() {
        return factureRepository.getStatistique();
    }

    @Override
    public Page<RecapClient> getRecapClient(Pageable pageable) {
        return factureRepository.getRecapClient(pageable);
    }

    @Override
    public List<Long> findAllIds(String refFacture, Double minMontatnTTC, Double maxMontatnTTC, Boolean paymentStatus, Long idClient, LocalDate dateDebut, LocalDate dateFin) {
        return factureRepository.findAllIds(refFacture, minMontatnTTC, maxMontatnTTC, paymentStatus, idClient, dateDebut, dateFin);
    }

    @Override
    public ResponseEntity<Void> deleteFacture(Long id) {
        if (id == null) {
            return null;
        }
        ligneFactureRepository.deleteByIdFacture(id);
        factureRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteLingeFacture(Long factureId, Long ligneFactureId) {
        FactureDto factureDto = findById(factureId);
        LigneFacture ligneFacture = ligneFactureRepository.findById(ligneFactureId).get();

        double timbre = timbreFiscalService.getTimbreFiscale().getMontant();

        Double montantHt = factureDto.getMontantHt();
        Double montantTTC = factureDto.getMontantTTC();

        montantHt -= ligneFacture.getPrixTotal();
        montantTTC = montantHt + (montantHt * 0.19) + (timbre / 1000);

        factureRepository.updateMontant(factureId, montantHt, montantTTC);

        this.ligneFactureRepository.deleteById(ligneFactureId);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> ajouterLingeFacture(Long factureId, Long idProduit, double prix, Integer quatite, Integer remise) {

        FactureDto factureDto = findById(factureId);
        Produit produit = produitRepository.findById(idProduit).get();

        LigneFacture ligneFacture = new LigneFacture();
        ligneFacture.setProduit(produit);
        ligneFacture.setRemise(remise);
        ligneFacture.setPrixUnitaire(prix * quatite);
        ligneFacture.setQuantite(quatite);
        ligneFacture.setPrixTotal((quatite * prix) - ((quatite * prix) * (remise / 100.0)));

        Facture f = new Facture();
        f.setId(factureDto.getId());
        ligneFacture.setFacture(f);
        ligneFactureRepository.save(ligneFacture);

        Double prixProduit = (quatite * prix) - ((quatite * prix) * (remise / 100.0));
        double timbre = timbreFiscalService.getTimbreFiscale().getMontant();

        Double montantHt = factureDto.getMontantHt() + prixProduit;
        Double montantTTC = montantHt + (montantHt * 0.19) + (timbre / 1000);

        factureRepository.updateMontant(factureId, montantHt, montantTTC);

        return ResponseEntity.ok().build();
    }

    @Override
    public Long creationDevis(Long factureId) {
        Facture facture = factureRepository.findById(factureId).orElseThrow(() -> new EntityNotFoundException("NOT_FOUND"));

        Devis devis = new Devis();
        devis.setReference(generateReferenceDevis());
        devis.setTauxTVA(facture.getTauxTVA());
        devis.setDateDevis(facture.getDateFacture());
        devis.setTimbreFiscale(facture.getTimbreFiscale());
        devis.setClient(facture.getClient());
        devis.setMontantHt(facture.getMontantHt());
        devis.setMontantTTC(facture.getMontantTTC());
        devis.setPaymentStatus(false);
        devis = devisRepository.save(devis);

        Devis finalDevis = devis;
        facture.getLignesFacture().forEach(l -> {
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

    public String generateReferenceDevis() {

        LocalDate today = LocalDate.now();
        int year = today.getYear();

        DecimalFormat decimalFormat = new DecimalFormat("000");
        Integer numDevis = numFactureRepository.getNumDevis();
        String nombreDeDevisFormatte = decimalFormat.format(numDevis + 1);
        numFactureRepository.updateNumDevis(numDevis + 1);
        return nombreDeDevisFormatte + "-" + year;
    }

    @Override
    public Long creationBonLivraison(Long factureId) {

        Facture facture = factureRepository.findById(factureId).orElseThrow(() -> new EntityNotFoundException("NOT_FOUND"));

        BondeLivraison bondeLivraison = new BondeLivraison();
        bondeLivraison.setReference(generateReferenceBonDeLivraison());
        bondeLivraison.setTauxTVA(facture.getTauxTVA());
        bondeLivraison.setDateBondeLivraison(facture.getDateFacture());
        bondeLivraison.setTimbreFiscale(facture.getTimbreFiscale());
        bondeLivraison.setClient(facture.getClient());
        bondeLivraison.setMontantHt(facture.getMontantHt());
        bondeLivraison.setMontantTTC(facture.getMontantTTC());
        bondeLivraison = bondeLivraisonRepository.save(bondeLivraison);

        BondeLivraison finalDevis = bondeLivraison;
        facture.getLignesFacture().forEach(l -> {
            LigneBondeLivraison ligneBondeLivraison = new LigneBondeLivraison();
            ligneBondeLivraison.setProduit(l.getProduit());
            ligneBondeLivraison.setRemise(l.getRemise());
            ligneBondeLivraison.setQuantite(l.getQuantite());
            ligneBondeLivraison.setPrixUnitaire(l.getPrixUnitaire());
            ligneBondeLivraison.setPrixTotal(l.getPrixTotal());
            ligneBondeLivraison.setBondeLivraison(finalDevis);
            ligneBondeLivraisonRepository.save(ligneBondeLivraison);
        });

        return bondeLivraison.getId();
    }

    public String generateReferenceBonDeLivraison() {

        LocalDate today = LocalDate.now();
        int year = today.getYear();

        DecimalFormat decimalFormat = new DecimalFormat("000");
        Integer numDevis = numFactureRepository.getNumDevis();
        String nombreDeDevisFormatte = decimalFormat.format(numDevis + 1);
        numFactureRepository.updateNumDevis(numDevis + 1);
        return nombreDeDevisFormatte + "-" + year;
    }
}
