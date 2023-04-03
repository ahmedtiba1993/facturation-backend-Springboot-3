package com.facturation.service;

import com.facturation.dto.ClientDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {

    Page<ClientDto> getAllClients(Pageable pageable);

    ClientDto getClientById(Long id);

    ClientDto createClient(ClientDto clientDto);

    void deleteClient(Long id);
}
