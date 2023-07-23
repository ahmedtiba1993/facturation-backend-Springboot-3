package com.facturation.validator;

import com.facturation.dto.FactureDto;
import com.facturation.model.Devis;

import java.util.ArrayList;
import java.util.List;

public class DevisValidator {
  public static List<String> validate(Devis dto) {
    List<String> errors = new ArrayList<>();

    if (dto.getClient().getId() == null) {
      errors.add("client est obligatoire");
    }
    if (dto.getLigneDevis().isEmpty()) {
      errors.add("produit est obligatoire");
    }
    if (dto.getDateDevis() == null) {
      errors.add("date est obligatoire");
    }
    if (dto.getPaymentStatus() == null) {
      errors.add("le statut de facture est obligatoire");
    }
    return errors;
  }
}
