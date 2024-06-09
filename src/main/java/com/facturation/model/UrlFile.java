package com.facturation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UrlFile {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name ="facutureId")
    private Facture facture;

    @ManyToOne
    @JoinColumn(name ="devisId")
    private Devis devis;

    @ManyToOne
    @JoinColumn(name ="bondeLivraisonId")
    private BondeLivraison bondeLivraison;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

}
