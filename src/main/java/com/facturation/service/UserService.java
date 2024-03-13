package com.facturation.service;

import com.facturation.dto.UserDto;
import com.facturation.user.User;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public interface UserService {
  User findByEmail(String email);

  ResponseEntity<HashMap<String, Object>> editPassword(Integer userId, String newPassword);

  UserDto getUserDtailsDto(String email);

  ResponseEntity<HashMap<String, Object>> modifierUserInfo(
      String firstName, String lastName, String email, Integer tel, Integer fax, Integer mobile);
}
