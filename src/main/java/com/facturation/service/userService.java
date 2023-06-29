package com.facturation.service;

import com.facturation.user.User;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public interface userService {
  User findByEmail(String email);

  ResponseEntity<HashMap<String, Object>> editPassword(Integer userId, String newPassword);
}
