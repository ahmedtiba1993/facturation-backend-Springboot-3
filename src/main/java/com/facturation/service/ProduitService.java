package com.facturation.service;

import com.facturation.dto.ProduitDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProduitService {

  ProduitDto save(ProduitDto dto);

  ProduitDto findById(Long id);

  void delete(Long id);

  List<ProduitDto> findAll();

  Page<ProduitDto> findAllPaginatedProductsWithFilter(
      Pageable pageable,
      String nom,
      String code,
      Double prixMin,
      Double prixMax,
      Boolean etatRemise);
}
