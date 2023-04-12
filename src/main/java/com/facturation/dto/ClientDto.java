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

    private String code;

    private String nomCommercial;

    private int remise;

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
                .code(client.getCode())
                .nomCommercial(client.getNomCommercial())
                .remise(client.getRemise())
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
        client.setCode(dto.getCode());
        client.setNomCommercial(dto.getNomCommercial());
        client.setRemise(dto.getRemise());
        return client;
    }
}
