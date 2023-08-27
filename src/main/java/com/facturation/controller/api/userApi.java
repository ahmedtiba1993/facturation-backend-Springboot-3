package com.facturation.controller.api;

import com.facturation.dto.UserDto;
import com.facturation.user.User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

import static com.facturation.utils.Constants.*;

public interface userApi {

  @GetMapping(value = USER_ENDPOINT + "/userConnected", produces = MediaType.APPLICATION_JSON_VALUE)
  User getUserByEmail(@RequestParam String email);

  @PostMapping(
      value = USER_ENDPOINT + "/changePassword",
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<HashMap<String, Object>> changePassword(
      @RequestParam Integer id, @RequestParam String newPassword);

  @GetMapping(value = USER_ENDPOINT + "/userInfo", produces = MediaType.APPLICATION_JSON_VALUE)
  UserDto getUserDtailsDto(@RequestParam String email);

  @PostMapping(
      value = USER_ENDPOINT + "/modifierUserInfo",
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<HashMap<String, Object>> modifierUserInfo(
      @RequestParam String firstName,
      @RequestParam String lastName,
      @RequestParam String email,
      @RequestParam Integer tel,
      @RequestParam Integer fax,
      @RequestParam Integer mobile);
}
