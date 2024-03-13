package com.facturation.dto;

import com.facturation.model.Client;
import com.facturation.model.Devis;
import com.facturation.model.Facture;
import com.facturation.model.LigneDevis;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class DevisDto {

    private Long id;

    private int tauxTVA;

    private Double montantTTC;

    private Double montantHt;

    private ClientDto client;

    private String reference;

    private double timbreFiscale;

    private LocalDate dateDevis;

    private List<LigneDevisDto> ligneDevis;

    private Boolean paymentStatus;

    private Boolean isFacture;

    public static DevisDto fromEntity(Devis devis) {

        if (devis == null) {
            return null;
        }

        return DevisDto.builder()
                .id(devis.getId())
                .montantHt(devis.getMontantHt())
                .montantTTC(devis.getMontantTTC())
                .tauxTVA(devis.getTauxTVA())
                .reference(devis.getReference())
                .client(ClientDto.fromEntity(devis.getClient()))
                .timbreFiscale(devis.getTimbreFiscale())
                .paymentStatus(devis.getPaymentStatus())
                .isFacture(devis.getIsFacture())
                .ligneDevis(
                        devis.getLigneDevis() != null
                                ? devis.getLigneDevis().stream()
                                .map(LigneDevisDto::fromEntity)
                                .collect(Collectors.toList())
                                : null)
                .dateDevis(devis.getDateDevis())
                .build();
    }

    public static Devis toEntity(DevisDto dto) {

        if (dto == null) {
            return null;
        }

        Devis devis = new Devis();
        devis.setId(dto.getId());
        devis.setMontantTTC(dto.getMontantTTC());
        devis.setMontantHt(dto.getMontantHt());
        devis.setTauxTVA(dto.getTauxTVA());
        devis.setReference(dto.getReference());
        devis.setClient(ClientDto.toEntity(dto.getClient()));
        devis.setTimbreFiscale(dto.getTimbreFiscale());
        devis.setDateDevis(dto.getDateDevis());
        devis.setPaymentStatus(dto.getPaymentStatus());
        devis.setIsFacture(dto.getIsFacture());
        return devis;
    }
}
