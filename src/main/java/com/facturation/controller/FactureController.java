package com.facturation.controller;

import com.facturation.controller.api.FactureApi;
import com.facturation.dto.FactureDto;
import com.facturation.service.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FactureController implements FactureApi {

    private FactureService factureService;

    @Autowired
    public FactureController(FactureService factureService) {
        this.factureService = factureService;
    }

    @Override
    public FactureDto save(FactureDto dto) {
        return factureService.save(dto);
    }

    @Override
    public Page<FactureDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return factureService.findAll(pageable);    }

    @Override
    public FactureDto findById(Long id) {
        return factureService.findById(id);
    }
}
