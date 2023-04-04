package com.facturation.dto;

import com.facturation.model.Facture;
import com.facturation.model.LigneFacture;
import com.facturation.model.Produit;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LigneFactureDto {

    private Long id;

    private FactureDto facture;

    private ProduitDto produit;

    private int quantite;

    private Double prixUnitaire;

    public static LigneFactureDto fromEntity(LigneFacture ligneFacture) {

        if(ligneFacture == null) {
            return null;
        }

        return LigneFactureDto.builder()
                .id(ligneFacture.getId())
                .facture(FactureDto.fromEntity(ligneFacture.getFacture()))
                .produit(ProduitDto.fromEntity(ligneFacture.getProduit()))
                .quantite(ligneFacture.getQuantite())
                .prixUnitaire(ligneFacture.getPrixUnitaire())
                .build();
    }

    public static LigneFacture toEntity(LigneFactureDto dto) {

        if(dto == null) {
            return null;
        }

        LigneFacture ligneFacture = new LigneFacture();
        ligneFacture.setId(dto.getId());
        ligneFacture.setFacture(FactureDto.toEntity(dto.getFacture()));
        ligneFacture.setProduit(ProduitDto.toEntity(dto.getProduit()));
        ligneFacture.setPrixUnitaire(dto.prixUnitaire);
        ligneFacture.setQuantite(dto.quantite);

        return ligneFacture;
    }
}
