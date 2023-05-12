package com.facturation.service;

import com.facturation.dto.ProduitDto;
import com.facturation.model.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProduitService {

    ProduitDto save(ProduitDto dto);

    ProduitDto findById(Long id);

    Page<ProduitDto> findAllPaginated(Pageable pageable);

    void delete (Long id);

    List<ProduitDto> findAll();


    Page<ProduitDto> filtrerProduits(Pageable pageable , String nom, String code, Double prixMin, Double prixMax, Boolean etatRemise);
}
