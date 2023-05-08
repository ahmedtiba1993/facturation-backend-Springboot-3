package com.facturation.dto;

import com.facturation.model.Client;
import com.facturation.model.Facture;
import com.facturation.model.LigneFacture;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class FactureDto {

    private Long id;

    private int tauxTVA;

    private Double montantTTC;

    private Double montantHt;

    private ClientDto client;

    private String reference;

    private double timbreFiscale;

    private LocalDate dateFacture;

    private List<LigneFactureDto> lignesFacture;

    private Boolean paymentStatus;

    public static FactureDto fromEntity (Facture facture) {

        if(facture == null) {
            return null;
        }

        return FactureDto.builder()
                .id(facture.getId())
                .montantHt(facture.getMontantHt())
                .montantTTC(facture.getMontantTTC())
                .tauxTVA(facture.getTauxTVA())
                .reference(facture.getReference())
                .client(ClientDto.fromEntity(facture.getClient()))
                .timbreFiscale(facture.getTimbreFiscale())
                .paymentStatus(facture.getPaymentStatus())
                .lignesFacture(
                        facture.getLignesFacture() !=null ?
                                facture.getLignesFacture().stream()
                                        .map(LigneFactureDto::fromEntity)
                                        .collect(Collectors.toList()) :null
                )
                .dateFacture(facture.getDateFacture())
                .build();
    }

    public static Facture toEntity(FactureDto dto) {

        if (dto == null) {
            return null;
        }

        Facture facture = new Facture();
        facture.setId(dto.getId());
        facture.setMontantTTC(dto.getMontantTTC());
        facture.setMontantHt(dto.getMontantHt());
        facture.setTauxTVA(dto.getTauxTVA());
        facture.setReference(dto.getReference());
        facture.setClient(ClientDto.toEntity(dto.getClient()));
        facture.setTimbreFiscale(dto.getTimbreFiscale());
        facture.setDateFacture(dto.getDateFacture());
        facture.setPaymentStatus(dto.getPaymentStatus());
        return facture;

    }

}
