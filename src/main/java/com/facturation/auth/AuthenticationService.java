package com.facturation.auth;

import com.facturation.config.JwtService;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.exception.UserNotFoundException;
import com.facturation.user.Role;
import com.facturation.user.User;
import com.facturation.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthenticationResponse register(RegisterRequest request) {
    Optional<User> findUser = repository.findByEmail(request.getEmail());
    if (!findUser.isEmpty()) {
      List<String> errors = new ArrayList<>();
      errors.add(
          "l'adresse email fourni existe déjà dans notre système. Veuillez choisir une adresse email uniques.");
      throw new InvalidEntityException(
          "Impossible de créer un nouvel utilisateur", ErrorCodes.USER_EXISTE, errors);
    }
    var user =
        User.builder()
            .firstname(request.getFirstname())
            .lastname(request.getLastname())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.ADMIN)
            .build();
    repository.save(user);
    var jwtToken = jwtService.generateToken(user);
    return AuthenticationResponse.builder().token(jwtToken).build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    Optional<User> optionalUser = repository.findByEmail(request.getEmail());
    User user = optionalUser.orElseThrow(() -> new UserNotFoundException("User not found"));

    var jwtToken = jwtService.generateToken(user);
    return AuthenticationResponse.builder().token(jwtToken).build();
  }
}
