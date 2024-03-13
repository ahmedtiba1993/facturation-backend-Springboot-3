package com.facturation.controller.api;

import com.facturation.model.Categorie;
import com.facturation.model.Produit;
import com.facturation.repository.CategorieRepository;
import com.facturation.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class test {

    @Autowired
    CategorieRepository categorieRepository;

    @Autowired
    ProduitRepository produitRepository;

    @GetMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Produit> test(@RequestBody List<Produit> products) {


        /*List<Produit> produits = produitRepository.findAll();

        List<Produit> exist = new ArrayList<>();
        for (int i = 0; i < produits.size(); i++) {
            int count = 0;
            for (int j = 0; j < produits.size(); j++) {
                if (produits.get(i).getCode().equals(produits.get(j).getCode())) {
                    count++;
                }
            }
            if (count > 1) {
                produitRepository.delete(produits.get(i));
                exist.add(produits.get(i));
            }
        }*/

        List<Produit> exist = produitRepository.saveAll(products);
        return exist;
    }
}
