package com.crm.kizuna.controllers;


import com.crm.kizuna.enums.UserType;
import com.crm.kizuna.models.Credential;
import com.crm.kizuna.models.User;
import com.crm.kizuna.requests.LoginRequest;
import com.crm.kizuna.requests.RegisterRequest;
import com.crm.kizuna.responses.AuthResponse;
import com.crm.kizuna.services.CredentialService;
import com.crm.kizuna.services.JwtService;
import com.crm.kizuna.services.PasswordService;
import com.crm.kizuna.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
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
      credential == null ||
        !passwordService.matches(request.getPassword(), credential.getHash())
    ) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "E-mail or password are incorrect!"
      );
    }

    AuthResponse authResponse = jwtService.generateToken(request.getEmail());
    return new AuthResponse(authResponse.expirationDate, user, authResponse.token);
  }

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

    AuthResponse authResponse = jwtService.generateToken(request.getEmail());
    return new AuthResponse(authResponse.expirationDate, newUser, authResponse.token);
  }
}
