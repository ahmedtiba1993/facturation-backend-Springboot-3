package com.facturation.service;

import com.facturation.dto.CategorieDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategorieService {

    CategorieDto save(CategorieDto dto);

    CategorieDto findById(Long id);

    Page<CategorieDto> findAll(Pageable pageable);

    void delete (Long id);
}
