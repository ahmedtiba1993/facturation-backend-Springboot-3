package com.facturation.dto;

import com.facturation.model.Client;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ClientDto {

    private Long id;

    private String nom;

    private String prenom;

    private String adresse;

    private int tel;

    public static ClientDto fromEntity(Client client) {
        if(client == null){
            return null;
        }
        return builder()
                .id(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .adresse(client.getAdresse())
                .tel(client.getTel())
                .build();
    }

    public static Client toEntity(ClientDto dto) {
        if(dto ==null){
            return null;
        }
        Client client = new Client();
        client.setId(dto.getId());
        client.setNom(dto.getNom());
        client.setPrenom(dto.getPrenom());
        client.setAdresse(dto.getAdresse());
        client.setTel(dto.getTel());
        return client;
    }
}
