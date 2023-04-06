package com.facturation.validator;

import com.facturation.dto.TimbreFiscalDto;

import java.util.ArrayList;
import java.util.List;

public class TimbreFiscaleValidator {

    public static List<String> validate(TimbreFiscalDto dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getMontant() <= 0) {
            errors.add("Le montant de timbre fiscale doit être supérieur à 0");
        }

        return errors;
    }
}
