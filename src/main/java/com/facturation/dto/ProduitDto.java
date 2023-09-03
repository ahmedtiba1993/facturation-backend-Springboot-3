package com.facturation.dto;

import com.facturation.model.Produit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProduitDto {

  private Long id;

  private String nom;

  private String description;

  private String code;

  private Double prix;

  private int stock;

  private CategorieDto category;

  private Boolean etatRemise;

  public ProduitDto(
      Long id,
      String nom,
      String description,
      String code,
      Double prix,
      int stock,
      Boolean etatRemise) {
    this.id = id;
    this.nom = nom;
    this.description = description;
    this.code = code;
    this.prix = prix;
    this.stock = stock;
    this.etatRemise = etatRemise;
  }

  public static ProduitDto fromEntity(Produit produit) {

    if (produit == null) {
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
        .etatRemise(produit.getEtatRemise())
        .build();
  }

  public static Produit toEntity(ProduitDto dto) {

    if (dto == null) {
      return null;
    }

    Produit produit = new Produit();
    produit.setId(dto.getId());
    produit.setNom(dto.getNom());
    produit.setDescription(dto.getDescription());
    produit.setCode(dto.getCode());
    produit.setPrix(dto.getPrix());
    produit.setStock(dto.getStock());
    produit.setEtatRemise(dto.getEtatRemise());
    produit.setCategorie(CategorieDto.toEntity(dto.getCategory()));

    return produit;
  }
}
