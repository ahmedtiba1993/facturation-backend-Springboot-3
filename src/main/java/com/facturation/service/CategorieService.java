package com.facturation.service;

import com.facturation.dto.CategorieDto;

import java.util.List;

public interface CategorieService {

    CategorieDto save(CategorieDto dto);

    CategorieDto findById(Long id);

    List<CategorieDto> findAll();

    void delete (Long id);
}
