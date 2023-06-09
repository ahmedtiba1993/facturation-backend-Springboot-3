package com.facturation.controller;

import com.facturation.controller.api.userApi;
import com.facturation.service.userService;
import com.facturation.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class userController implements userApi {

  private userService userService;

  @Autowired
  public userController(com.facturation.service.userService userService) {
    this.userService = userService;
  }

  @Override
  public User getUserByEmail(String email) {
    return userService.findByEmail(email);
  }

  @Override
  public ResponseEntity<HashMap<String, Object>> changePassword(Integer id, String newPassword) {
    return userService.editPassword(id, newPassword);
  }
}
