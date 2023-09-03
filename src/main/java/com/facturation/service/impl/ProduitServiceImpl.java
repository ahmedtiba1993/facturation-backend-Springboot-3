package com.facturation.service.impl;

import com.facturation.dto.ProduitDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.Produit;
import com.facturation.repository.ProduitRepository;
import com.facturation.service.ProduitService;
import com.facturation.validator.ProduitValidator;
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
public class ProduitServiceImpl implements ProduitService {

  private ProduitRepository produitRepository;

  @Autowired
  public ProduitServiceImpl(ProduitRepository produitRepository) {
    this.produitRepository = produitRepository;
  }

  @Override
  public ProduitDto save(ProduitDto dto) {
    List<String> errors = ProduitValidator.validate(dto);

    if (!errors.isEmpty()) {
      log.error("Produit is not valid {} ", dto);
      throw new InvalidEntityException(
          "Produit n est pas valide", ErrorCodes.PRODUIT_NOT_VALID, errors);
    }
    // pour modification
    if (dto.getId() != null) {
      Optional<Produit> produit = produitRepository.findById(dto.getId());
      if (dto.getCode().equals(produit.get().getCode()) == false) {
        Optional<Produit> produitByCode = produitRepository.findProduitByCode(dto.getCode());
        if (produitByCode.isPresent()) {
          log.error("Produit is not valid {} ", dto);
          errors.add("Code produit existe");
          throw new InvalidEntityException(
              "Produit n est pas valide", ErrorCodes.PRODUIT_NOT_VALID, errors);
        }
      }
      return ProduitDto.fromEntity(produitRepository.save(ProduitDto.toEntity(dto)));
    }
    // ---------------------
    Optional<Produit> produit = produitRepository.findProduitByCode(dto.getCode());
    if (produit.isPresent()) {
      log.error("Produit is not valid {} ", dto);
      errors.add("Code produit existe");
      throw new InvalidEntityException(
          "Produit n est pas valide", ErrorCodes.PRODUIT_NOT_VALID, errors);
    }

    log.info("Produit{} ", dto);
    return ProduitDto.fromEntity(produitRepository.save(ProduitDto.toEntity(dto)));
  }

  @Override
  public ProduitDto findById(Long id) {
    if (id == null) {
      return null;
    }

    Optional<Produit> prodtuit = produitRepository.findById(id);
    ProduitDto dto = prodtuit.map(ProduitDto::fromEntity).orElse(null);

    if (dto == null) {
      throw new EntityNotFoundException(
          "Aucune produit trouvée dans la base de données", ErrorCodes.PRODUIT_NOT_FOUND);
    }

    return dto;
  }

  @Override
  public void delete(Long id) {
    if (id == null) {
      return;
    }

    produitRepository.deleteById(id);
  }

  @Override
  public List<ProduitDto> findAll() {
    return produitRepository.findAllProduits();
  }

  @Override
  public Page<ProduitDto> findAllPaginatedProductsWithFilter(
      Pageable pageable,
      String nom,
      String code,
      Double prixMin,
      Double prixMax,
      Boolean etatRemise) {
    Page<ProduitDto> produits =
        produitRepository.findAllProduitsAsDto(pageable, nom, code, prixMin, prixMax, etatRemise);
    return produits;
  }
}
