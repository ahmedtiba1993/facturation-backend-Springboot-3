package com.facturation.validator;

import com.facturation.dto.BondeLivraisonDto;
import com.facturation.model.BondeLivraison;

import java.util.ArrayList;
import java.util.List;

public class BondeLivraisonValidator {

    public static List<String> validate(BondeLivraison dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getClient() == null) {
            errors.add("client est obligatoire");
        }
        if (dto.getLigneBondeLivraisons() == null || dto.getLigneBondeLivraisons().isEmpty()) {
            errors.add("produit est obligatoire");
        }
        if (dto.getDateBondeLivraison() == null) {
            errors.add("date est obligatoire");
        }

        return errors;
    }
}
