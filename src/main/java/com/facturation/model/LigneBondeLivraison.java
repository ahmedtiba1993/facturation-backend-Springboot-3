package com.facturation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LigneBondeLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantite;

    private Double prixUnitaire;

    private int remise;

    private double prixTotal;

    @ManyToOne
    private BondeLivraison bondeLivraison;

    @ManyToOne private Produit produit;

}
