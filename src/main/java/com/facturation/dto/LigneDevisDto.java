package com.facturation.dto;

import com.facturation.model.LigneDevis;
import com.facturation.model.LigneFacture;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LigneDevisDto {
  private Long id;

  private DevisDto devis;

  private ProduitDto produit;

  private int quantite;

  private double prixUnitaire;

  private int remise;

  private double prixTotal;

  public static LigneDevisDto fromEntity(LigneDevis ligneDevis) {

    if (ligneDevis == null) {
      return null;
    }

    return LigneDevisDto.builder()
        .id(ligneDevis.getId())
        // .facture(FactureDto.fromEntity(ligneFacture.getFacture()))
        .produit(ProduitDto.fromEntity(ligneDevis.getProduit()))
        .quantite(ligneDevis.getQuantite())
        .prixUnitaire(ligneDevis.getPrixUnitaire())
        .remise(ligneDevis.getRemise())
        .prixTotal(ligneDevis.getPrixTotal())
        .build();
  }

  public static LigneDevis toEntity(LigneDevisDto dto) {

    if (dto == null) {
      return null;
    }

    LigneDevis ligneDevis = new LigneDevis();
    ligneDevis.setId(dto.getId());
    // ligneFacture.setFacture(FactureDto.toEntity(dto.getFacture()));
    ligneDevis.setProduit(ProduitDto.toEntity(dto.getProduit()));
    ligneDevis.setPrixUnitaire(dto.prixUnitaire);
    ligneDevis.setQuantite(dto.quantite);
    ligneDevis.setRemise(dto.getRemise());
    ligneDevis.setPrixTotal(dto.getPrixTotal());
    return ligneDevis;
  }
}
