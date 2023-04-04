package com.facturation.validator;

import com.facturation.dto.ProduitDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProduitValidator {

    public static List<String> validate(ProduitDto produit) {
        List<String> errors = new ArrayList<>();

        // Validation du nom du produit
        if (produit.getNom() == null || produit.getNom().isEmpty()) {
            errors.add("Le nom de produit est obligatoire");
        }

        if (produit.getDescription() == null || produit.getDescription().isEmpty()) {
            errors.add("La desciption de produit est obligatoire");
        }

        if (produit.getCode() == null || produit.getCode().isEmpty()) {
            errors.add("Le code de produit est obligatoire");
        }

      /*  // Validation de la catégorie du produit
       if (produit.getCategory() == null) {
            errors.add("Le produit doit être associé à une catégorie");
        }*/

        // Validation du prix unitaire du produit
        if (produit.getPrix() == null || produit.getPrix() <= 0) {
            errors.add("Le prix unitaire du produit doit être supérieur à 0");
        }

        if (produit.getStock() <= 0) {
            errors.add("Le stock doit être supérieur à 0");
        }

        return errors;
    }
}
