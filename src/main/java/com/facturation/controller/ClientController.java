package com.facturation.controller;

import com.facturation.controller.api.ClientApi;
import com.facturation.dto.ClientDto;
import com.facturation.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientController implements ClientApi {

    private ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public ClientDto save(ClientDto dto) {
        return clientService.createClient(dto);
    }

    @Override
    public ClientDto findById(Long id) {
        return clientService.getClientById(id);
    }

    @Override
    public Page<ClientDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clientService.getAllClients(pageable);
    }

    @Override
    public void delete(Long id) {
        clientService.deleteClient(id);
    }
}
