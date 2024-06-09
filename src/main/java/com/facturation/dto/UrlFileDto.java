package com.facturation.dto;

import com.facturation.model.UrlFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UrlFileDto {

    private UUID uuid;

    public static UrlFileDto toUrlFileDto(UrlFile urlFile){
        return UrlFileDto.builder()
                .uuid(urlFile.getUuid())
                .build();
    }

}
