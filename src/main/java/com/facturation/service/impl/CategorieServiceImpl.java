package com.facturation.service.impl;

import com.facturation.dto.CategorieDto;
import com.facturation.dto.ProduitDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.exception.OperationNotAllowedException;
import com.facturation.model.Categorie;
import com.facturation.model.Produit;
import com.facturation.repository.CategorieRepository;
import com.facturation.repository.ProduitRepository;
import com.facturation.service.CategorieService;
import com.facturation.validator.CategorieValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository categorieRepository;
    private final ProduitRepository produitRepository;

    @Autowired
    public CategorieServiceImpl(CategorieRepository categorieRepository, ProduitRepository produitRepository) {
        this.categorieRepository = categorieRepository;
        this.produitRepository = produitRepository;
    }

    @Override
    public CategorieDto save(CategorieDto dto) {
        List<String> errors = CategorieValidator.validate(dto);

        if (!errors.isEmpty()) {
            throw new InvalidEntityException("Catégorie n est pas valide", ErrorCodes.CATEGORIE_NOT_VALID, errors);
        }

        if (categorieRepository.findByNom(dto.getNom()).isPresent()) {
            errors.add("La catégorie " + dto.getNom() + " existe déjà.");
            throw new InvalidEntityException("La catégorie " + dto.getNom() + " existe déjà.", ErrorCodes.CATEGORIE_NOT_VALID, errors);
        }

        return CategorieDto.fromEntity(categorieRepository.save(CategorieDto.toEntity(dto)));
    }

    @Override
    public CategorieDto findById(Long id) {
        if (id == null) {
            return null;
        }

        Optional<Categorie> categories = categorieRepository.findById(id);
        CategorieDto dto = categories.map(CategorieDto::fromEntity).orElse(null);

        if (dto == null) {
            throw new EntityNotFoundException("Aucune catégorie trouvée dans la base de données",
                    ErrorCodes.CATEGORIE_NOT_FOUND);
        }

        return dto;
    }

    @Override
    public Page<CategorieDto> findAll(Pageable pageable) {
        Page<Categorie> categories = categorieRepository.findAll(pageable);
        Function<Categorie, CategorieDto> converter = CategorieDto::fromEntity;
        Page<CategorieDto> categorieDtosPage = categories.map(converter);
        return categorieDtosPage;
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        if (produitRepository.countByCategorieId(id) > 0) {
            throw new OperationNotAllowedException("La catégorie contient des produits, elle ne peut pas être supprimée");
        }
        categorieRepository.deleteById(id);
    }
}
