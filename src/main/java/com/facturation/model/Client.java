package com.facturation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Client implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String prenom;

    private String nomCommercial;

    private String adresse;

    private int tel;

    private String code;

    private int remise;

    @OneToMany(mappedBy = "client",fetch = FetchType.LAZY)
    private List<Facture> factures;

}
