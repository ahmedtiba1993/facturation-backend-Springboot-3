package com.facturation.dto;

import com.facturation.model.Client;
import com.facturation.model.LigneDevis;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class DevisDto {

  private Long id;

  private LocalDate dateDevis;

  private int tauxTVA;

  private Double montantTTC;

  private Double montantHt;

  private String reference;

  private double timbreFiscale;

  private Boolean paymentStatus;

  private ClientDto clientDto;

  private List<LigneDevis> ligneDevis;

  public DevisDto(
      Long id,
      LocalDate dateDevis,
      int tauxTVA,
      Double montantTTC,
      Double montantHt,
      String reference,
      double timbreFiscale,
      Boolean paymentStatus,
      Long clientId,
      String clientNom,
      String clientPrenom) {
    this.id = id;
    this.dateDevis = dateDevis;
    this.tauxTVA = tauxTVA;
    this.montantTTC = montantTTC;
    this.montantHt = montantHt;
    this.reference = reference;
    this.timbreFiscale = timbreFiscale;
    this.paymentStatus = paymentStatus;
    this.clientDto = ClientDto.builder().id(clientId).nom(clientNom).prenom(clientPrenom).build();
  }

  public DevisDto(
      Long id,
      LocalDate dateDevis,
      int tauxTVA,
      Double montantTTC,
      Double montantHt,
      String reference,
      double timbreFiscale,
      Boolean paymentStatus,
      Client client) {
    this.id = id;
    this.dateDevis = dateDevis;
    this.tauxTVA = tauxTVA;
    this.montantTTC = montantTTC;
    this.montantHt = montantHt;
    this.reference = reference;
    this.timbreFiscale = timbreFiscale;
    this.paymentStatus = paymentStatus;
  }
}
