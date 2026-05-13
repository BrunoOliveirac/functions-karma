package com.crm.karma.controllers;


import com.crm.karma.enums.UserType;
import com.crm.karma.models.Credential;
import com.crm.karma.models.User;
import com.crm.karma.requests.LoginRequest;
import com.crm.karma.requests.RegisterRequest;
import com.crm.karma.responses.AuthResponse;
import com.crm.karma.services.CredentialService;
import com.crm.karma.services.JwtService;
import com.crm.karma.services.PasswordService;
import com.crm.karma.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

  private final JwtService jwtService;
  private final UserService userService;
  private final PasswordService passwordService;
  private final CredentialService credentialService;

  public AuthController(JwtService jwtService, UserService userService, PasswordService passwordService, CredentialService credentialService) {
    this.jwtService = jwtService;
    this.userService = userService;
    this.passwordService = passwordService;
    this.credentialService = credentialService;
  }

  /**
   * Authenticates the user in the system
   *
   * @param request The object with e-mail and password data
   * @return Returns the user object, the token, and his expiration date when successful. Otherwise, returns UNAUTHORIZED when a user with e-mail provided is already exists
   */
  @Operation(summary = "Login an user")
  @PostMapping("/login")
  public AuthResponse login(@RequestBody LoginRequest request) {
    User user = userService.getByEmail(request.getEmail());

    if (user == null) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "E-mail or password are incorrect!"
      );
    }

    Credential credential = credentialService.getByUserId(user.getId());

    if (
      credential == null || !passwordService.matches(request.getPassword(), credential.getHash())
    ) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "E-mail or password are incorrect!"
      );
    }

    AuthResponse authResponse = jwtService.generateToken(request.getEmail(), List.of(user.getType()));
    return new AuthResponse(authResponse.expirationDate, user, authResponse.token);
  }

  /**
   * Create a new user and sign in him
   *
   * @param request An object with name, email, and password attributes
   * @return Returns the user object, the token, and his expiration date when successful. Otherwise, returns UNAUTHORIZED when a user with e-mail provided is already exists
   */
  @Operation(summary = "Create a new user")
  @PostMapping("/register")
  public AuthResponse register(@RequestBody RegisterRequest request) {
    User existsUser = userService.getByEmail(request.getEmail());

    if (existsUser != null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail already exists!");
    }

    User newUser = User.builder()
      .name(request.getName())
      .email(request.getEmail())
      .type(UserType.USER).build();

    User createdUser = userService.add(newUser);

    String hash = passwordService.hashPassword(request.getPassword());
    credentialService.add(hash, createdUser.getId());

    AuthResponse authResponse = jwtService.generateToken(request.getEmail(), List.of(UserType.USER));
    return new AuthResponse(authResponse.expirationDate, newUser, authResponse.token);
  }
}
