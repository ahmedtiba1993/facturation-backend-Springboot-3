package com.facturation.model.projection;

public interface Statistique {
  Double getMontantFacturePaye();

  Double getMontantFactureNonPaye();

  Long getNbFacturePaye();

  Long getNbFactureNonPaye();

  Double getMontantDevisPaye();

  Double getMontantDevisNonPaye();

  Long getNbDevisPaye();

  Long getNbDevisNonPaye();

  Long getNbClient();
}
