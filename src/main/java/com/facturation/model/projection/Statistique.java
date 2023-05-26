package com.facturation.model.projection;

public interface Statistique {
    Double getMontatPaye();
    Double getMontantNonPaye();
    Integer getNbFacturePaye();
    Integer getNbFactureNonPaye();
    Integer getNbClient();
}
