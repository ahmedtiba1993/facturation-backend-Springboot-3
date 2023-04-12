package com.facturation.service;

import com.facturation.dto.FactureDto;
import com.facturation.dto.ProduitDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FactureService {

    FactureDto save(FactureDto dto);

    String generateReference(String codeClient);

    Page<FactureDto> findAll(Pageable pageable);

    FactureDto findById(Long id);

    }
