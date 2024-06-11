package com.facturation.dto;

import com.facturation.model.BondeLivraison;
import com.facturation.model.UrlFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UrlFileDto {

    private UUID uuid;
    private FactureDto factureDto;
    private DevisDto devisDto;
    private BondeLivraisonDto bondeLivraisonDto;

    public static UrlFileDto toUrlFileDto(UrlFile urlFile){
        return UrlFileDto.builder()
                .uuid(urlFile.getUuid())
                .factureDto(FactureDto.fromEntity(urlFile.getFacture()))
                .devisDto(DevisDto.fromEntity(urlFile.getDevis()))
                .bondeLivraisonDto(BondeLivraisonDto.fromEntity(urlFile.getBondeLivraison()))
                .build();
    }

}
