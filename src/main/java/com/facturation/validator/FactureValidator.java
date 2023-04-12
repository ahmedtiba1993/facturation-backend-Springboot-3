package com.facturation.validator;

import com.facturation.dto.FactureDto;
import com.facturation.dto.ProduitDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FactureValidator {
    public static List<String> validate(FactureDto dto) {
        List<String> errors = new ArrayList<>();


        if (dto.getClient().getId() == null) {
            errors.add("client est obligatoire");
        }
        if(dto.getLignesFacture().isEmpty()){
            errors.add("produit est obligatoire");
        }
        if(dto.getDateFacture() == null){
            errors.add("date est obligatoire");
        }
        return errors;

    }
}