package com.facturation.model.projection;

public interface ClientRecapProjection {
  Long getIdClient();

  String getNomClient();

  String getPrenomClient();

  String getNomCommercial();

  Long getNumFacture();

  Double getNmontantNonPaye();

  Double getNmontantPaye();
}
