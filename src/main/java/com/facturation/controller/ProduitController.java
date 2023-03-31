package com.facturation.controller;

import com.facturation.controller.api.ProduitApi;
import com.facturation.dto.ProduitDto;
import com.facturation.service.ProduitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProduitController implements ProduitApi {

    ProduitService produitService;

    @Autowired
    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @Override
    public ProduitDto save(ProduitDto dto) {
        return produitService.save(dto);
    }

    @Override
    public ProduitDto findById(Long id) {
        return produitService.findById(id);
    }

    @Override
    public List<ProduitDto> findAll() {
        return produitService.findAll();
    }

    @Override
    public void delete(Long id) {
        produitService.delete(id);
    }
}
