package com.facturation.controller;

import com.facturation.controller.api.FactureApi;
import com.facturation.dto.FactureDto;
import com.facturation.service.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
