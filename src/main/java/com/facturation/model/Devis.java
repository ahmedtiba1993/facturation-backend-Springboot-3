package com.facturation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Devis {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDate dateDevis;

  private int tauxTVA;

  private Double montantTTC;

  private Double montantHt;

  private String reference;

  private double timbreFiscale;

  private Boolean paymentStatus;

  @ManyToOne private Client client;

  @OneToMany(mappedBy = "devis", fetch = FetchType.LAZY)
  private List<LigneDevis> ligneDevis;
}
