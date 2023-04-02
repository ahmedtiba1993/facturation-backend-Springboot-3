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
        log.info("Produit{} ",dto);
        List<String> errors = ProduitValidator.validate(dto);

        if(!errors.isEmpty()) {
            log.error("Produit is not valid {} ",dto);
            throw new InvalidEntityException("Produit n est pas valide", ErrorCodes.PRODUIT_NOT_VALID,errors);
        }
        log.info("Produit{} ",dto);
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
    public Page<ProduitDto> findAll(Pageable pageable) {
        Page<Produit> produits = produitRepository.findAll(pageable);
        Function<Produit, ProduitDto> converter = ProduitDto::fromEntity;
        Page<ProduitDto> produitDtosPage = produits.map(converter);
        return produitDtosPage;
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }

        produitRepository.deleteById(id);
    }
}
