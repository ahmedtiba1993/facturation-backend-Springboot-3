package com.facturation.validator;

import com.facturation.dto.ClientDto;

import java.util.ArrayList;
import java.util.List;

public class ClientValidator {

    public static List<String> validate(ClientDto clientDto) {
        List<String> errors = new ArrayList<>();

        if (clientDto == null) {
            errors.add("Le client ne peut pas être null");
        } else {
            // Vérification de la longueur du nom
            if (clientDto.getNom() == null || clientDto.getNom().length() > 50) {
                errors.add("Le nom doit avoir moins de 50 caractères");
            }

            // Vérification de la longueur du prénom
            if (clientDto.getPrenom() == null || clientDto.getPrenom().length() > 50) {
                errors.add("Le prénom doit avoir moins de 50 caractères");
            }

            // Vérification de l'adresse
            if (clientDto.getAdresse() == null || clientDto.getAdresse().length() > 255) {
                errors.add("L'adresse doit avoir moins de 255 caractères");
            }

            // Vérification du numéro de téléphone
            if (clientDto.getTel() <= 0) {
                errors.add("Le numéro de téléphone doit être supérieur à 0");
            }
        }

        return errors;
    }
}
