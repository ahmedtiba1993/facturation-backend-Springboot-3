package com.facturation.service.impl;

import com.facturation.dto.DevisDto;
import com.facturation.dto.FactureDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.*;
import com.facturation.model.projection.ClientRecapProjection;
import com.facturation.repository.*;
import com.facturation.service.DevisService;
import com.facturation.service.TimbreFiscalService;
import com.facturation.validator.DevisValidator;
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
@Slf4j
public class DevisServiceImpl implements DevisService {

  @Autowired private DevisRepository devisRepository;
  @Autowired private ClientRepository clientRepository;
  @Autowired private ProduitRepository produitRepository;
  @Autowired private LigneDevisRepository ligneDevisRepository;
  @Autowired private TimbreFiscalService timbreFiscalService;
  @Autowired private NumFactureRepository numFactureRepository;
  @Autowired private TvaRepository tvaRepository;

  @Override
  public DevisDto save(Devis devis) {

    List<String> errors = DevisValidator.validate(devis);

    if (!errors.isEmpty()) {
      log.error("Devis is not valid {} ", devis);
      throw new InvalidEntityException(
          "Devis n est pas valide", ErrorCodes.FACTURE_NOT_VALID, errors);
    }

    Optional<Client> client = clientRepository.findById(devis.getClient().getId());
    if (client.isEmpty()) {
      log.error("Client not fond dans devis ", devis.getClient().getId());
      throw new EntityNotFoundException("Aucune client trouvée");
    }

    List<String> produitErrors = new ArrayList<String>();
    if (devis.getLigneDevis() != null) {
      for (LigneDevis ligneDevis : devis.getLigneDevis()) {
        if (ligneDevis.getProduit() != null) {
          Optional<Produit> produit = produitRepository.findById(ligneDevis.getProduit().getId());
          if (produit.isEmpty()) {
            log.error("Produit not found dans facture ", ligneDevis.getProduit().getId());
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
    devis.setTauxTVA(tauxTva);
    devis.setReference(generateReference());
    devis.setTimbreFiscale(timbre);
    Devis saveDevis = devisRepository.save(devis);

    double montantTotalProduit = 0.0;
    if (devis.getLigneDevis() != null) {
      for (LigneDevis ligneDevis : devis.getLigneDevis()) {
        int remise = ligneDevis.getRemise();
        double montantProduit = ligneDevis.getProduit().getPrix() * ligneDevis.getQuantite();
        if (ligneDevis.getProduit().getEtatRemise() == true) {
          montantTotalProduit =
              montantTotalProduit + (montantProduit - (montantProduit * (remise / 100.0)));
        } else {
          montantTotalProduit = montantTotalProduit + montantProduit;
        }
        ligneDevis.setDevis(saveDevis);
        ligneDevis.setPrixUnitaire(ligneDevis.getProduit().getPrix());
        ligneDevis.setRemise(remise);
        ligneDevis.setPrixTotal((montantProduit - (montantProduit * (remise / 100.0))));
        ligneDevisRepository.save(ligneDevis);
      }
    }
    double montantTotal =
        montantTotalProduit + (montantTotalProduit * (tauxTva / 100.0)) + (timbre / 1000);
    devisRepository.updateMontantTotal(saveDevis.getId(), montantTotalProduit, montantTotal);
    return DevisDto.fromEntity(saveDevis);
  }

  @Override
  public String generateReference() {

    LocalDate today = LocalDate.now();
    int year = today.getYear();

    DecimalFormat decimalFormat = new DecimalFormat("000");
    Integer numDevis = numFactureRepository.getNumDevis();
    String nombreDeDevisFormatte = decimalFormat.format(numDevis + 1);
    numFactureRepository.updateNumDevis(numDevis + 1);
    return nombreDeDevisFormatte + "-" + year;
  }

  @Override
  public Page<DevisDto> findAll(
      Pageable pageable,
      String refdevis,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin) {
    Page<Devis> devis =
        devisRepository.findAllFiltre(
            pageable,
            refdevis,
            minMontatnTTC,
            maxMontatnTTC,
            paymentStatus,
            idClient,
            dateDebut,
            dateFin);
    Function<Devis, DevisDto> converter = DevisDto::fromEntity;
    Page<DevisDto> devisDtosPage = devis.map(converter);
    return devisDtosPage;
  }

  @Override
  public DevisDto findById(Long id) {
    if (id == null) {
      return null;
    }

    Optional<Devis> devis = devisRepository.findById(id);
    DevisDto dto = devis.map(DevisDto::fromEntity).orElse(null);

    if (dto == null) {
      throw new EntityNotFoundException(
          "Aucune facture trouvée dans la base de données", ErrorCodes.FACTURE_NOT_FOUND);
    }

    return dto;
  }

  @Override
  public ResponseEntity<InputStreamResource> generatePdf(List<Long> ids)
      throws DocumentException, IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    Document document = new Document();
    PdfWriter.getInstance(document, outputStream);

    document.open();
    List<Devis> devisList = devisRepository.findDevisToPdf(ids);

    for (Devis devis : devisList) {
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
      Paragraph f1 = new Paragraph("Devis : " + devis.getReference());
      f1.setLeading(0, 1.5f);

      Paragraph f2 = new Paragraph("Date: " + devis.getDateDevis());
      f2.setLeading(0, 1.5f);

      celltable1.setBorder(Rectangle.NO_BORDER);
      celltable1.addElement(f1);
      celltable1.addElement(f2);
      table.addCell(celltable1);

      // deuxième colonne : informations du client
      PdfPCell celltable2 = new PdfPCell();
      Paragraph f3 = new Paragraph("Client: " + devis.getClient().getNomCommercial());
      f3.setLeading(0, 1.5f);

      Paragraph f4 = new Paragraph("Adresse: " + devis.getClient().getAdresse());
      f4.setLeading(0, 1.5f);

      Paragraph f5 = new Paragraph("MF: " + devis.getClient().getCode());
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
      for (LigneDevis l : devis.getLigneDevis()) {

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
          new PdfPCell(new Phrase(String.valueOf(df.format(devis.getMontantHt()) + " TND")));
      cellTotalBrutValue.setBorder(Rectangle.NO_BORDER);
      cellTotalBrutValue.setPaddingTop(20);
      cellTotalBrutValue.setPaddingBottom(7);
      tablePrix.addCell(cellTotalBrutValue);

      PdfPCell cellTVA = new PdfPCell(new Phrase("TVA 19 %"));
      cellTVA.setBorder(Rectangle.NO_BORDER);
      cellTVA.setPaddingBottom(7);
      tablePrix.addCell(cellTVA);

      PdfPCell cellTVAValue =
          new PdfPCell(new Phrase(String.valueOf(df.format(devis.getMontantHt() * 0.19)) + " TND"));
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
                  String.valueOf(df.format(devis.getMontantTTC())) + " TND",
                  new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD)));
      cellTotalTTCValue.setBorder(Rectangle.NO_BORDER);
      tablePrix.addCell(cellTotalTTCValue);
      tableFacture.setSpacingAfter(30);
      document.add(tablePrix); // ajoute la table au document

      int partieEntiere = (int) Math.floor(devis.getMontantTTC());
      double partieDecimale = devis.getMontantTTC() - Math.floor(devis.getMontantTTC());
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
  public ResponseEntity<Void> updateStatus(Long id) {
    if (id == null) {
      return null;
    }

    Optional<Devis> devis = devisRepository.findById(id);

    if (!devis.isPresent()) {
      throw new EntityNotFoundException(
          "Aucune facture trouvée dans la base de données", ErrorCodes.FACTURE_NOT_FOUND);
    }
    if (devis.get().getPaymentStatus() != null && devis.get().getPaymentStatus()) {
      devisRepository.setStatusFalse(id);
    } else {
      devisRepository.setStatusTrue(id);
    }

    return ResponseEntity.ok().build();
  }

  @Override
  public List<Long> findAllIds(
      String refDevis,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin) {
    return devisRepository.findAllIds(
        refDevis, minMontatnTTC, maxMontatnTTC, paymentStatus, idClient, dateDebut, dateFin);
  }

  @Override
  public ResponseEntity<Void> deleteDevis(Long id) {
    if (id == null) {
      return null;
    }
    ligneDevisRepository.deleteByIdDevis(id);
    devisRepository.deleteById(id);
    return ResponseEntity.ok().build();
  }

  @Override
  public Page<ClientRecapProjection> getRecapClient(Pageable pageable) {
    return devisRepository.getRecapClient(pageable);
  }
}
