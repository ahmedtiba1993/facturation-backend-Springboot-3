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
public class BondeLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateBondeLivraison;

    private int tauxTVA;

    private Double montantTTC;

    private Double montantHt;

    private String reference;

    private double timbreFiscale;

    @ManyToOne
    private Client client;

    @OneToMany(mappedBy = "bondeLivraison", fetch = FetchType.LAZY)
    private List<LigneBondeLivraison> ligneBondeLivraisons;
}
