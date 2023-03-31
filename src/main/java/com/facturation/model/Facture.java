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
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateFacture;

    private Double montantTotal;

    @ManyToOne
    private Client client;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    private List<LigneFacture> lignesFacture;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;
}
