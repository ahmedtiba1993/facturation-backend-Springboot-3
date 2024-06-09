package com.facturation.dto;

import com.facturation.model.LigneBondeLivraison;
import com.facturation.model.LigneDevis;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LigneBondeLivraisonDto {

    private Long id;

    private BondeLivraisonDto bondeLivraison;

    private ProduitDto produit;

    private int quantite;

    private double prixUnitaire;

    private int remise;

    private double prixTotal;

    public static LigneBondeLivraisonDto fromEntity(LigneBondeLivraison ligneBondeLivraison) {

        if (ligneBondeLivraison == null) {
            return null;
        }

        return LigneBondeLivraisonDto.builder()
                .id(ligneBondeLivraison.getId())
                .produit(ProduitDto.fromEntity(ligneBondeLivraison.getProduit()))
                .quantite(ligneBondeLivraison.getQuantite())
                .prixUnitaire(ligneBondeLivraison.getPrixUnitaire())
                .remise(ligneBondeLivraison.getRemise())
                .prixTotal(ligneBondeLivraison.getPrixTotal())
                .build();
    }

    public static LigneBondeLivraison toEntity(LigneBondeLivraisonDto dto) {

        if (dto == null) {
            return null;
        }

        LigneBondeLivraison ligneBondeLivraison = new LigneBondeLivraison();
        ligneBondeLivraison.setId(dto.getId());
        // ligneFacture.setFacture(FactureDto.toEntity(dto.getFacture()));
        ligneBondeLivraison.setProduit(ProduitDto.toEntity(dto.getProduit()));
        ligneBondeLivraison.setPrixUnitaire(dto.prixUnitaire);
        ligneBondeLivraison.setQuantite(dto.quantite);
        ligneBondeLivraison.setRemise(dto.getRemise());
        ligneBondeLivraison.setPrixTotal(dto.getPrixTotal());
        return ligneBondeLivraison;
    }
}
