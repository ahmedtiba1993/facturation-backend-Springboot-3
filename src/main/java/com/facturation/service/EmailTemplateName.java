package com.facturation.service;

import lombok.Getter;

@Getter
public enum EmailTemplateName {

    FACTURE_TEMPLATE("facture"),
    DEVIS_TEMPLATE("devis");
    ;

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
