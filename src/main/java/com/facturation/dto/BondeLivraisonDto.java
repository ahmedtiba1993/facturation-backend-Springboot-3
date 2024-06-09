package com.facturation.dto;

import com.facturation.model.BondeLivraison;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
public class BondeLivraisonDto {

    private Long id;

    private int tauxTVA;

    private Double montantTTC;

    private Double montantHt;

    private ClientDto client;

    private String reference;

    private double timbreFiscale;

    private LocalDate dateBondeLivraison;

    private List<LigneBondeLivraisonDto> ligneBondeLivraison;

    public static BondeLivraisonDto fromEntity(BondeLivraison bondeLivraison) {

        if (bondeLivraison == null) {
            return null;
        }

        return BondeLivraisonDto.builder()
                .id(bondeLivraison.getId())
                .montantHt(bondeLivraison.getMontantHt())
                .montantTTC(bondeLivraison.getMontantTTC())
                .tauxTVA(bondeLivraison.getTauxTVA())
                .reference(bondeLivraison.getReference())
                .client(ClientDto.fromEntity(bondeLivraison.getClient()))
                .timbreFiscale(bondeLivraison.getTimbreFiscale())
                .ligneBondeLivraison(
                        bondeLivraison.getLigneBondeLivraisons() != null
                                ? bondeLivraison.getLigneBondeLivraisons().stream()
                                .map(LigneBondeLivraisonDto::fromEntity)
                                .collect(Collectors.toList())
                                : null)
                .dateBondeLivraison(bondeLivraison.getDateBondeLivraison())
                .build();
    }

    public static BondeLivraison toEntity(BondeLivraisonDto dto) {

        if (dto == null) {
            return null;
        }

        BondeLivraison bondeLivraison = new BondeLivraison();
        bondeLivraison.setId(dto.getId());
        bondeLivraison.setMontantTTC(dto.getMontantTTC());
        bondeLivraison.setMontantHt(dto.getMontantHt());
        bondeLivraison.setTauxTVA(dto.getTauxTVA());
        bondeLivraison.setReference(dto.getReference());
        bondeLivraison.setClient(ClientDto.toEntity(dto.getClient()));
        bondeLivraison.setTimbreFiscale(dto.getTimbreFiscale());
        return bondeLivraison;
    }
}