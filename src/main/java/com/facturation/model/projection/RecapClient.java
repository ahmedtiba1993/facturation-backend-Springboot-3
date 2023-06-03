package com.facturation.model.projection;

public interface RecapClient {
    Long getIdClient();
    String getNomClient();
    String getPrenomClient();
    String getNomCommercial();
    Integer getNumFacture();
    Double getNmontantNonPaye();
    Double getNmontantPaye();
}
