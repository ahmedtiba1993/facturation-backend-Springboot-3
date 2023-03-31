package com.facturation.dto;

import com.facturation.model.Produit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class ProduitDto {

    private Long id;

    private String nom;

    private String description;

    private String code;

    private BigDecimal prix;

    private int stock;

    private CategorieDto category;


    public static ProduitDto fromEntity(Produit produit) {

        if(produit == null) {
            return null;
        }
        return ProduitDto.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .description(produit.getDescription())
                .code(produit.getCode())
                .prix(produit.getPrix())
                .stock(produit.getStock())
                .category(CategorieDto.fromEntity(produit.getCategorie()))
                .build();
    }

    public static Produit toEntity(ProduitDto dto) {

        if(dto == null) {
            return null;
        }
        
        Produit produit = new Produit();
            produit.setId(dto.getId());
            produit.setNom(dto.getNom());
            produit.setDescription(dto.getDescription());
            produit.setCode(dto.getCode());
            produit.setPrix(dto.getPrix());
            produit.setStock(dto.getStock());
            produit.setCategorie(CategorieDto.toEntity(dto.getCategory()));

        return produit;
    }
}
