package com.facturation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserDto {
  private String firstname;
  private String lastname;
  private String email;
  private Integer tel;
  private Integer fax;
  private Integer mobile;
}
