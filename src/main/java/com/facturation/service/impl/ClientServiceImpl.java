package com.facturation.service.impl;

import com.facturation.dto.CategorieDto;
import com.facturation.dto.ClientDto;
import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.Client;
import com.facturation.repository.ClientRepository;
import com.facturation.service.ClientService;
import com.facturation.validator.ClientValidator;
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
public class ClientServiceImpl implements ClientService {

  private ClientRepository clientRepository;

  @Autowired
  public ClientServiceImpl(ClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  @Override
  public ClientDto createClient(ClientDto dto) {
    List<String> errors = ClientValidator.validate(dto);

    if (!errors.isEmpty()) {
      log.error("Client is not valid {} ", dto);
      throw new InvalidEntityException(
          "Client n est pas valide", ErrorCodes.CLINET_NOT_VALID, errors);
    }

    // pour modification
    if (dto.getId() != null) {
      Optional<Client> client = clientRepository.findById(dto.getId());
      if (dto.getCode().equals(client.get().getCode()) == false) {
        Optional<Client> clientCode = clientRepository.findClientsByCode(dto.getCode());
        if (clientCode.isPresent()) {
          log.error("Client is not valid {} ", dto);
          errors.add("Code client existe");
          throw new InvalidEntityException(
              "Client n est pas valide", ErrorCodes.CLINET_NOT_VALID, errors);
        }
      }
    } else {
      Optional<Client> client = clientRepository.findClientsByCode(dto.getCode());
      if (client.isPresent()) {
        log.error("Client is not valid {} ", dto);
        errors.add("Code client existe");
        throw new InvalidEntityException(
            "Client n est pas valide", ErrorCodes.CLINET_NOT_VALID, errors);
      }
    }
    log.info("Add Client{} ", dto);
    return ClientDto.fromEntity(clientRepository.save(ClientDto.toEntity(dto)));
  }

  @Override
  public Page<ClientDto> getAllClientsPaginated(Pageable pageable) {
    Page<Client> clients = clientRepository.findAll(pageable);
    Function<Client, ClientDto> converter = ClientDto::fromEntity;
    Page<ClientDto> clientDtosPage = clients.map(converter);
    return clientDtosPage;
  }

  @Override
  public List<ClientDto> getAllClient() {
    return clientRepository.findAll().stream()
        .map(ClientDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public ClientDto getClientById(Long id) {
    if (id == null) {
      return null;
    }

    Optional<Client> client = clientRepository.findById(id);
    ClientDto dto = client.map(ClientDto::fromEntity).orElse(null);

    if (dto == null) {
      throw new EntityNotFoundException(
          "Aucune client trouvée dans la base de données", ErrorCodes.CLIENT_NOT_FOUND);
    }

    return dto;
  }

  @Override
  public void deleteClient(Long id) {
    if (id == null) {
      return;
    }
    clientRepository.deleteById(id);
  }
}
