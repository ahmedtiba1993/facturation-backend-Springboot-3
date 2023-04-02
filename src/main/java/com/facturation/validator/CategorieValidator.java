package com.facturation.validator;

import com.facturation.dto.CategorieDto;
import com.facturation.dto.ProduitDto;

import java.util.ArrayList;
import java.util.List;

public class CategorieValidator {

    public static List<String> validate(CategorieDto dto) {
        List<String> errors = new ArrayList<>();

        // Validation du nom du produit
        if (dto.getNom() == null || dto.getNom().isEmpty()) {
            errors.add("Le nom du categorie ne peut pas être vide");
        }

        if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
            errors.add("La desciption du produit ne peut pas être vide");
        }

        return errors;
    }
}
