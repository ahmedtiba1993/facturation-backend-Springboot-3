package com.facturation.dto;

import com.facturation.model.Categorie;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CategorieDto {


    private Long id;

    private String nom;

    private String description;

    public static CategorieDto fromEntity(Categorie categorie) {
        if(categorie==null){
            return null;
        }
        return CategorieDto.builder()
                .id(categorie.getId())
                .nom(categorie.getNom())
                .description(categorie.getDescription())
                .build();
    }

    public static Categorie toEntity(CategorieDto dto) {
        if(dto == null) {
            return null;
        }
        Categorie categorie = new Categorie();
        categorie.setId(dto.getId());
        categorie.setNom(dto.getNom());
        categorie.setDescription(dto.getDescription());
        return categorie;
    }
}
