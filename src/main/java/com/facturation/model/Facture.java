package com.facturation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

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

    @CreatedDate
    private LocalDate dateFacture;

    private int tauxTVA;

    private Double montantTTC;

    private Double montantHt;

    private String reference;

    private double timbreFiscale;

    @ManyToOne
    private Client client;

    @OneToMany(mappedBy = "facture")
    private List<LigneFacture> lignesFacture;

}
