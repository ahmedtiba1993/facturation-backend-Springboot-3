package com.facturation.dto;

import com.facturation.model.TimbreFiscal;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TimbreFiscalDto {

    private Long id;

    private double montant;

    private String code;

    public static TimbreFiscalDto fromEntity(TimbreFiscal timbreFiscal) {
        if(timbreFiscal == null){
            return null;
        }

        return TimbreFiscalDto.builder()
                .id(timbreFiscal.getId())
                .montant(timbreFiscal.getMontant())
                .code(timbreFiscal.getCode())
                .build();
    }

    public static TimbreFiscal toEntity(TimbreFiscalDto dto) {
        if(dto == null){
            return null;
        }

        return TimbreFiscal.builder()
                .id(dto.getId())
                .montant(dto.getMontant())
                .code(dto.getCode())
                .build();
    }

}
