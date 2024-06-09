package com.facturation.service.impl;

import com.facturation.dto.FactureDto;
import com.facturation.dto.UrlFileDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.model.BondeLivraison;
import com.facturation.model.Devis;
import com.facturation.model.Facture;
import com.facturation.model.UrlFile;
import com.facturation.repository.BondeLivraisonRepository;
import com.facturation.repository.DevisRepository;
import com.facturation.repository.FactureRepository;
import com.facturation.repository.UrlFileRepository;
import com.facturation.service.FactureService;
import com.facturation.service.UrlFileService;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlFileServiceImpl implements UrlFileService {

    private final FactureRepository factureRepository;
    private final UrlFileRepository urlFileRepository;
    private final FactureService factureService;
    private final DevisRepository devisRepository;
    private final BondeLivraisonRepository bondeLivraisonRepository;

    @Override
    public UrlFileDto createUrlFile(Long id, String type) {
        UrlFile urlFile = new UrlFile();
        UrlFile verifExist = urlFileRepository.verifExist(id);
        if(verifExist != null){
            return UrlFileDto.toUrlFileDto(verifExist);
        }
        if(type.equals("facture")){
            Facture facture = factureRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("NOT_FOUND"));
            urlFile.setFacture(facture);
        }else if(type.equals("devis")){
            Devis devis = devisRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("NOT_FOUND"));
            urlFile.setDevis(devis);
        }else {
            BondeLivraison bondeLivraison = bondeLivraisonRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("NOT_FOUND"));
            urlFile.setBondeLivraison(bondeLivraison);
        }
        return UrlFileDto.toUrlFileDto(urlFileRepository.save(urlFile));
    }

    @Override
    public FactureDto getFactureId(UUID uuid){
        UrlFile urlFile = urlFileRepository.findByUuid(uuid).orElseThrow(() -> new EntityNotFoundException("NOT_FOUND"));
        return FactureDto.fromEntity(urlFile.getFacture());
    }

    @Override
    public ResponseEntity<InputStreamResource> generatePdf(Long id) throws DocumentException, IOException {
        Facture facture = factureRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("NOT_FOUND"));
        var pdf = factureService.generatePdf(List.of(facture.getId()));
        return pdf;
    }

}
