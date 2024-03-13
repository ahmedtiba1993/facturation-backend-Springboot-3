package com.facturation.controller;

import com.facturation.controller.api.userApi;
import com.facturation.dto.UserDto;
import com.facturation.service.UserService;
import com.facturation.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class userController implements userApi {

  private UserService userService;

  @Autowired
  public userController(UserService userService) {
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

  @Override
  public UserDto getUserDtailsDto(String email) {
    return userService.getUserDtailsDto(email);
  }

  @Override
  public ResponseEntity<HashMap<String, Object>> modifierUserInfo(
      String firstName, String lastName, String email, Integer tel, Integer fax, Integer mobile) {
    return userService.modifierUserInfo(firstName, lastName, email, tel, fax, mobile);
  }
}
