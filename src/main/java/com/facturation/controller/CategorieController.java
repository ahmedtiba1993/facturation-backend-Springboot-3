package com.facturation.controller;

import com.facturation.controller.api.CategorieApi;
import com.facturation.dto.CategorieDto;
import com.facturation.service.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategorieController implements CategorieApi {

    private CategorieService categorieService;

    @Autowired
    public CategorieController(CategorieService categorieService) {
        this.categorieService = categorieService;
    }

    @Override
    public CategorieDto save(CategorieDto dto) {
        return categorieService.save(dto);
    }

    @Override
    public CategorieDto findById(Long id) {
        return categorieService.findById(id);
    }

    @Override
    public Page<CategorieDto> findAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categorieService.findAllPaginated(pageable);
    }

    @Override
    public void delete(Long id) {
        categorieService.delete(id);
    }

    @Override
    public List<CategorieDto> findAll() {
        return categorieService.findAll();
    }
}
