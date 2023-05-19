package com.facturation.utils;

public class MontantEnLettres {
    private static final String[] unités = { "", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf" };
    private static final String[] dizaines = { "", "", "vingt", "trente", "quarante", "cinquante", "soixante", "soixante", "quatre-vingt", "quatre-vingt" };

    public static void main(String[] args) {
        double montant = 1234.56;
        String montantEnLettres = convertirEnLettres(montant);
        System.out.println(montantEnLettres);
    }

    public static String convertirEnLettres(double montant) {
        int partieEntière = (int) montant;
        int partieDécimale = (int) Math.round((montant - partieEntière) * 100);

        String enLettres = "";

        if (partieEntière == 0) {
            enLettres += "zéro";
        } else if (partieEntière < 0) {
            enLettres += "moins " + convertirPartieEntièreEnLettres(Math.abs(partieEntière));
        } else {
            enLettres += convertirPartieEntièreEnLettres(partieEntière);
        }

        if (partieDécimale > 0) {
            enLettres += " " + convertirPartieEntièreEnLettres(partieDécimale);
        }

        return enLettres;
    }

    public static String convertirPartieEntièreEnLettres(int partieEntière) {
        String enLettres = "";

        if (partieEntière >= 1000000) {
            enLettres += convertirPartieEntièreEnLettres(partieEntière / 1000000) + " million ";
            partieEntière %= 1000000;
        }

        if (partieEntière >= 1000) {
            enLettres += convertirPartieEntièreEnLettres(partieEntière / 1000) + " mille ";
            partieEntière %= 1000;
        }

        if (partieEntière >= 100) {
            enLettres += convertirPartieEntièreEnLettres(partieEntière / 100) + " cent ";
            partieEntière %= 100;
        }

        if (partieEntière >= 20) {
            enLettres += dizaines[partieEntière / 10] + "-";
            partieEntière %= 10;
        }

        if (partieEntière > 0) {
            enLettres += unités[partieEntière];
        }

        return enLettres;
    }
}



