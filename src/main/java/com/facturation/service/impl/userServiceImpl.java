package com.facturation.service.impl;

import com.facturation.dto.UserDto;
import com.facturation.service.UserService;
import com.facturation.user.User;
import com.facturation.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

@Service
@Slf4j
public class userServiceImpl implements UserService {

  UserRepository userRepository;
  private PasswordEncoder passwordEncoder;

  @Autowired
  public userServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User findByEmail(String email) {
    Optional<User> user = userRepository.findByEmail(email);
    return user.get();
  }

  @Override
  public ResponseEntity<HashMap<String, Object>> editPassword(Integer userId, String newPassword) {

    ResponseEntity<HashMap<String, Object>> response = null;
    HashMap<String, Object> dataRespenseObject = new HashMap<String, Object>();

    if (userId == null) {
      dataRespenseObject.put("success", false);
      dataRespenseObject.put("code", "PARAMS_MISSING");
      dataRespenseObject.put("message", "Id est manque.");
      response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);
      return response;
    }

    Optional<User> user = userRepository.findById(userId);
    if (!user.isPresent()) {
      dataRespenseObject.put("success", false);
      dataRespenseObject.put("code", "USER_NOT_FOUND");
      dataRespenseObject.put("message", "Ce utilisateur n'est pas trouvé.");
      response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);
      return response;
    }

    String password = passwordEncoder.encode(newPassword);
    userRepository.updatePassword(userId, password);
    dataRespenseObject.put("success", true);
    response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);
    return response;
  }

  @Override
  public UserDto getUserDtailsDto(String email) {
    if (email == null) {
      return null;
    }
    UserDto userDto = userRepository.findUserByEmail(email);
    return userDto;
  }

  @Override
  public ResponseEntity<HashMap<String, Object>> modifierUserInfo(
      String firstName, String lastName, String email, Integer tel, Integer fax, Integer mobile) {

    ResponseEntity<HashMap<String, Object>> response = null;
    HashMap<String, Object> dataRespenseObject = new HashMap<String, Object>();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = new User();
    if (authentication != null && authentication.isAuthenticated()) {
      // Vous pouvez maintenant accéder aux informations de l'utilisateur
      user = (User) authentication.getPrincipal();
    }
    if (!user.getEmail().equals(email)) {
      UserDto verifEmail = userRepository.findUserByEmail(email);
      if (verifEmail == null) {
        dataRespenseObject.put("success", false);
        dataRespenseObject.put("code", "EMAIL_EXIST");
        dataRespenseObject.put("message", "EMAIL_EXIST.");
        response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);
      }
    } else {
      userRepository.modifierUserInfo(user.getId(), firstName, lastName, email, tel, fax, mobile);
      dataRespenseObject.put("success", true);
      response = new ResponseEntity<HashMap<String, Object>>(dataRespenseObject, HttpStatus.OK);
    }
    return response;
  }
}
