package com.facturation.service.impl;

import com.facturation.dto.FactureDto;
import com.facturation.dto.LigneFactureDto;
import com.facturation.dto.UserDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.Client;
import com.facturation.model.Facture;
import com.facturation.model.LigneFacture;
import com.facturation.model.Produit;
import com.facturation.model.projection.RecapClient;
import com.facturation.model.projection.Statistique;
import com.facturation.repository.*;
import com.facturation.service.FactureService;
import com.facturation.service.TimbreFiscalService;
import com.facturation.user.User;
import com.facturation.user.UserRepository;
import com.facturation.validator.FactureValidator;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfLayer;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
public class FactureServiceImpl implements FactureService {

  @Autowired private FactureRepository factureRepository;

  @Autowired private ClientRepository clientRepository;

  @Autowired private ProduitRepository produitRepository;

  @Autowired private LigneFactureRepository ligneFactureRepository;

  @Autowired private TimbreFiscalService timbreFiscalService;

  @Autowired private NumFactureRepository numFactureRepository;

  @Autowired private TvaRepository tvaRepository;

  @Autowired private UserRepository userRepository;

  @Override
  public FactureDto save(FactureDto dto) {
    List<String> errors = FactureValidator.validate(dto);

    if (!errors.isEmpty()) {
      log.error("Facture is not valid {} ", dto);
      throw new InvalidEntityException(
          "Facture n est pas valide", ErrorCodes.FACTURE_NOT_VALID, errors);
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
          Optional<Produit> produit =
              produitRepository.findById(ligneFactureDto.getProduit().getId());
          if (produit.isEmpty()) {
            log.error("Produit not found dans facture ", ligneFactureDto.getProduit().getId());
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
    dto.setTauxTVA(tauxTva);
    dto.setReference(generateReference(client.get().getCode()));
    dto.setTimbreFiscale(timbre);
    Facture saveFacture = factureRepository.save(FactureDto.toEntity(dto));

    double montantTotalProduit = 0.0;
    if (dto.getLignesFacture() != null) {
      for (LigneFactureDto ligneFact : dto.getLignesFacture()) {
        int remise = ligneFact.getRemise();
        double montantProduit = ligneFact.getPrixUnitaire() * ligneFact.getQuantite();
        if (ligneFact.getRemise() > 0) {
          montantTotalProduit =
              montantTotalProduit + (montantProduit - (montantProduit * (remise / 100.0)));
        } else {
          montantTotalProduit = montantTotalProduit + montantProduit;
        }
        LigneFacture ligneFacture = LigneFactureDto.toEntity(ligneFact);
        ligneFacture.setFacture(saveFacture);
        ligneFacture.setPrixUnitaire(ligneFact.getPrixUnitaire());
        ligneFacture.setRemise(remise);
        ligneFacture.setPrixTotal((montantProduit - (montantProduit * (remise / 100.0))));
        ligneFactureRepository.save(ligneFacture);
      }
    }
    double montantTotal =
        montantTotalProduit + (montantTotalProduit * (tauxTva / 100.0)) + (timbre / 1000);
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
  public Page<FactureDto> findAll(
      Pageable pageable,
      String refFacture,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin) {
    Page<Facture> factures =
        factureRepository.findAllFiltre(
            pageable,
            refFacture,
            minMontatnTTC,
            maxMontatnTTC,
            paymentStatus,
            idClient,
            dateDebut,
            dateFin);
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
      throw new EntityNotFoundException(
          "Aucune facture trouvée dans la base de données", ErrorCodes.FACTURE_NOT_FOUND);
    }

    return dto;
  }

  @Override
  public ResponseEntity<InputStreamResource> generatePdf(List<Long> ids)
      throws DocumentException, IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = new User();
    if (authentication != null && authentication.isAuthenticated()) {
      // Vous pouvez maintenant accéder aux informations de l'utilisateur
      user = (User) authentication.getPrincipal();
    }

    List<Facture> factureList = factureRepository.findFactureToPdf(ids);

    Document document = new Document();
    PdfWriter writer = PdfWriter.getInstance(document, outputStream);

    document.open();
    for (Facture facture : factureList) {
      document.newPage();
      document.setMargins(36, 36, 0, 0); // Les marges sont définies à zéro

      Image img = Image.getInstance("classpath:logofacture.png");
      img.scalePercent(35);
      img.setScaleToFitLineWhenOverflow(true);
      // Positionner l'image à gauche de la page
      float marginLeft = document.leftMargin(); // Récupérer la marge gauche du document
      float imageX = 10;
      ; // Décalage de 20 unités de la marge gauche
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

      String millions = user.getTel().toString().substring(0, 1);
      String centMille = user.getTel().toString().substring(1, 4);
      String mille = user.getTel().toString().substring(4);

      // deuxième colonne : informations du client
      PdfPCell celltableHeader2 = new PdfPCell();
      Paragraph p6 =
          new Paragraph(
              "Téléphone: "
                  + user.getTel().toString().substring(0, 2)
                  + " "
                  + user.getTel().toString().substring(2, 4)
                  + " "
                  + user.getTel().toString().substring(4));
      p6.setLeading(0, 1.5f);
      Paragraph p7 =
          new Paragraph(
              "Fax: "
                  + user.getFax().toString().substring(0, 2)
                  + " "
                  + user.getFax().toString().substring(2, 4)
                  + " "
                  + user.getFax().toString().substring(4));
      p7.setLeading(0, 1.5f);
      Paragraph p8 =
          new Paragraph(
              "Mobile: "
                  + user.getMobile().toString().substring(0, 2)
                  + " "
                  + user.getMobile().toString().substring(2, 4)
                  + " "
                  + user.getMobile().toString().substring(4));
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
      Paragraph f1 = new Paragraph("facture: " + facture.getReference());
      f1.setLeading(0, 1.5f);

      Paragraph f2 = new Paragraph("Date: " + facture.getDateFacture());
      f2.setLeading(0, 1.5f);

      celltable1.setBorder(Rectangle.NO_BORDER);
      celltable1.addElement(f1);
      celltable1.addElement(f2);
      table.addCell(celltable1);

      // deuxième colonne : informations du client
      PdfPCell celltable2 = new PdfPCell();
      Paragraph f3 = new Paragraph("Client: " + facture.getClient().getNomCommercial());
      f3.setLeading(0, 1.5f);

      Paragraph f4 = new Paragraph("Adresse: " + facture.getClient().getAdresse());
      f4.setLeading(0, 1.5f);

      Paragraph f5 = new Paragraph("MF: " + facture.getClient().getCode());
      f5.setLeading(0, 1.5f);

      celltable2.setBorder(Rectangle.NO_BORDER);
      celltable2.addElement(f3);
      celltable2.addElement(f4);
      celltable2.addElement(f5);
      celltable2.setPaddingLeft(30);
      table.addCell(celltable2);

      table.setSpacingAfter(5f);
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
      for (LigneFacture l : facture.getLignesFacture()) {
        int padding = 7;

        PdfPCell cell = new PdfPCell(new Phrase(l.getProduit().getCode()));
        cell.setPadding(padding);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableFacture.addCell(cell);

        PdfPCell cell2 = new PdfPCell(new Phrase(l.getProduit().getDescription()));
        cell2.setPadding(padding);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableFacture.addCell(cell2);

        PdfPCell cell3 = new PdfPCell(new Phrase(String.valueOf(l.getQuantite())));
        cell3.setPadding(padding);
        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableFacture.addCell(cell3);

        PdfPCell cell4 =
            new PdfPCell(new Phrase(String.valueOf(df.format(l.getPrixUnitaire()) + " TND")));
        cell4.setPadding(padding);
        cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableFacture.addCell(cell4);

        PdfPCell cell5 =
            new PdfPCell(new PdfPCell(new Phrase(String.valueOf(l.getRemise()) + "%")));
        cell5.setPadding(padding);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableFacture.addCell(cell5);

        PdfPCell cell6 =
            new PdfPCell(new PdfPCell(new Phrase(df.format(l.getPrixTotal()) + " TND")));
        cell6.setPadding(padding);
        cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
        tableFacture.addCell(cell6);
      }

      // Ajout du tableau au document
      tableFacture.setSpacingAfter(20f);

      document.add(tableFacture);

      int partieEntiere = (int) Math.floor(facture.getMontantTTC());
      double partieDecimale = facture.getMontantTTC() - Math.floor(facture.getMontantTTC());
      int resultat = (int) (partieDecimale * 1000);

      // Créez un calque pour le texte en arrière-plan
      PdfLayer backgroundLayer = new PdfLayer("Background Text", writer);
      backgroundLayer.setOnPanel(false);
      backgroundLayer.setPrint("Print", false);

      // Activez le calque du texte en arrière-plan

      writer.lockLayer(backgroundLayer);

      // Ajoutez du texte principal
      Font font = new Font(Font.FontFamily.HELVETICA, 12);
      Paragraph paragraph =
          new Paragraph(
              convertirEnLettres(partieEntiere) + " dinars et (" + resultat + ") millimes", font);

      // Créez un tableau avec 1 ligne et 2 colonnes
      PdfPTable table5 = new PdfPTable(2);
      table5.setWidthPercentage(100);

      // Première colonne
      PdfPCell cell1 = new PdfPCell(paragraph);
      cell1.addElement(new Paragraph("Arrêté la présente facture à la somme de :"));
      cell1.addElement(paragraph);
      cell1.setHorizontalAlignment(Element.ALIGN_CENTER);

      // Supprimer les bordures de la cellule
      cell1.setBorderColor(BaseColor.WHITE);
      cell1.setBorderWidth(0);
      cell1.setPaddingRight(30);
      table5.addCell(cell1);

      // Deuxième colonne
      PdfPCell cell2 = new PdfPCell();
      cell2.setHorizontalAlignment(Element.ALIGN_CENTER);

      // Créez un paragraphe avec un interligne de 1,5
      Paragraph paragraph6 = new Paragraph();
      paragraph6.setLeading(20f); // Espacement de 1,5 fois la taille de la police

      paragraph6.add(new Phrase("Total brut HT : " + df.format(facture.getMontantHt()) + " TND"));
      paragraph6.add(Chunk.NEWLINE);

      paragraph6.add(new Phrase("TVA 19 % : " + df.format(facture.getMontantHt() * 0.19) + " TND"));
      paragraph6.add(Chunk.NEWLINE);

      paragraph6.add(new Phrase("Droit de timbre : 1.000 TND"));
      paragraph6.add(Chunk.NEWLINE);

      cell2.addElement(paragraph6);

      Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
      Paragraph boldParagraph =
          new Paragraph("Total TTC : " + df.format(facture.getMontantTTC()) + " TND", boldFont);
      cell2.addElement(boldParagraph);

      // Supprimer les bordures de la cellule
      cell2.setBorderColor(BaseColor.WHITE);
      cell2.setBorderWidth(0);
      table5.addCell(cell2);

      // Ajoutez le tableau au document
      document.add(table5);
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

    Optional<Facture> facture = factureRepository.findById(id);

    if (!facture.isPresent()) {
      throw new EntityNotFoundException(
          "Aucune facture trouvée dans la base de données", ErrorCodes.FACTURE_NOT_FOUND);
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
  public List<Long> findAllIds(
      String refFacture,
      Double minMontatnTTC,
      Double maxMontatnTTC,
      Boolean paymentStatus,
      Long idClient,
      LocalDate dateDebut,
      LocalDate dateFin) {
    return factureRepository.findAllIds(
        refFacture, minMontatnTTC, maxMontatnTTC, paymentStatus, idClient, dateDebut, dateFin);
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
  public ResponseEntity<Void> ajouterLingeFacture(
      Long factureId, Long idProduit, double prix, Integer quatite, Integer remise) {

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
}
