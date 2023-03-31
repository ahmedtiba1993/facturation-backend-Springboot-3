package com.facturation.service.impl;

import com.facturation.dto.CategorieDto;
import com.facturation.dto.ProduitDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.Categorie;
import com.facturation.model.Produit;
import com.facturation.repository.ProduitRepository;
import com.facturation.service.ProduitService;
import com.facturation.validator.ProduitValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

        if(!errors.isEmpty()) {
            log.error("Produit is not valid {} ",dto);
            throw new InvalidEntityException("Produit n est pas valide", ErrorCodes.PRODUIT_NOT_VALID,errors);
        }
        log.error("");
        return ProduitDto.fromEntity(produitRepository.save(ProduitDto.toEntity(dto)));
    }

    @Override
    public ProduitDto findById(Long id) {
        if ( id == null) {
            return null;
        }

        Optional<Produit> prodtuit = produitRepository.findById(id);
        ProduitDto dto = prodtuit.map(ProduitDto::fromEntity).orElse(null);

        if (dto == null) {
            throw new EntityNotFoundException("Aucune produit trouvée dans la base de données",
                    ErrorCodes.PRODUIT_NOT_FOUND);
        }

        return dto;
    }

    @Override
    public List<ProduitDto> findAll() {
        return produitRepository.findAll().stream()
                .map(ProduitDto::fromEntity)
                .collect(Collectors.toList());    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }

        produitRepository.deleteById(id);
    }
}
