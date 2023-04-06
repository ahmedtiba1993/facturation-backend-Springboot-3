package com.facturation.exception;

public enum ErrorCodes {
    USER_NOT_FOUND(1),
    USER_NOT_VALID(2),
    USER_EXISTE(3),
    /*Produit*/
    PRODUIT_NOT_FOUND(10),
    PRODUIT_NOT_VALID(11),
    CATEGORIE_NOT_FOUND(20),
    CATEGORIE_NOT_VALID(21),
    CLINET_NOT_VALID(30),
    CLIENT_NOT_FOUND(31), FACTURE_NOT_VALID(40),
    TIMBRE_FISCALE_NOT_VALID(50);

    private int code;

    ErrorCodes(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }

}
