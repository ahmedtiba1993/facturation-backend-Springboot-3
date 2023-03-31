package com.facturation.service;

import com.facturation.dto.ProduitDto;

import java.util.List;

public interface ProduitService {

    ProduitDto save(ProduitDto dto);

    ProduitDto findById(Long id);

    List<ProduitDto> findAll();

    void delete (Long id);
}
