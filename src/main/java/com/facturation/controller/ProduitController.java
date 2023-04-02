package com.facturation.controller;

import com.facturation.controller.api.ProduitApi;
import com.facturation.dto.ProduitDto;
import com.facturation.model.Produit;
import com.facturation.service.ProduitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Page<ProduitDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return produitService.findAll(pageable);
    }

    @Override
    public void delete(Long id) {
        produitService.delete(id);
    }
}
