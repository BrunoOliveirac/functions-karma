package com.crm.karma.controllers;


import com.crm.karma.enums.UserType;
import com.crm.karma.models.Credential;
import com.crm.karma.models.User;
import com.crm.karma.requests.LoginRequest;
import com.crm.karma.requests.RegisterRequest;
import com.crm.karma.services.CredentialService;
import com.crm.karma.services.JwtService;
import com.crm.karma.services.PasswordService;
import com.crm.karma.services.TokenBlacklistService;
import com.crm.karma.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
  private final TokenBlacklistService tokenBlacklistService;

  public AuthController(
    JwtService jwtService,
    UserService userService,
    PasswordService passwordService,
    CredentialService credentialService,
    TokenBlacklistService tokenBlacklistService
  ) {
    this.jwtService = jwtService;
    this.userService = userService;
    this.passwordService = passwordService;
    this.credentialService = credentialService;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  /**
   * Authenticates the user in the system.
   * Allows up to {@link UserService#MAX_LOGIN_ATTEMPTS} consecutive failures;
   * afterwards the account is locked for {@link UserService#LOGIN_LOCK_DURATION}.
   *
   * @param request The object with e-mail and password data
   * @return Returns the token when successful.
   *         UNAUTHORIZED for invalid credentials;
   *         TOO_MANY_REQUESTS when the account is locked.
   */
  @Operation(summary = "Login an user")
  @PostMapping("/login")
  public String login(@RequestBody LoginRequest request) {
    User user = userService.getByEmail(request.getEmail());

    if (user == null || !Boolean.TRUE.equals(user.getActive())) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "E-mail or password are incorrect!"
      );
    }

    if (userService.isLoginLocked(user)) {
      throw new ResponseStatusException(
        HttpStatus.TOO_MANY_REQUESTS,
        "Account temporarily locked due to too many failed login attempts!"
      );
    }

    userService.clearExpiredLoginLock(user);
    Credential credential = credentialService.getByUserId(user.getId());

    if (
      credential == null || !passwordService.matches(request.getPassword(), credential.getHash())
    ) {
      boolean locked = userService.registerFailedLogin(user);

      if (locked) {
        throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS,
          "Account temporarily locked due to too many failed login attempts!"
        );
      }

      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "E-mail or password are incorrect!"
      );
    }

    userService.resetLoginAttempts(user);

    return jwtService.generateToken(request.getEmail(), List.of(user.getType()));
  }

  /**
   * Invalidates the current JWT so it cannot be reused until its natural expiration.
   * Idempotent: missing/invalid/expired tokens are ignored and still return NO_CONTENT.
   *
   * @param authHeader Authorization header with the Bearer token
   */
  @Operation(summary = "Logout and revoke the current token")
  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return;
    }

    String token = authHeader.substring(7);

    try {
      tokenBlacklistService.revoke(token);
    } catch (Exception ignored) {
      // Token already expired or invalid — nothing left to revoke.
    }
  }

  /**
   * Create a new user and sign in him
   *
   * @param request An object with name, email, and password attributes
   * @return Returns the token when successful. Otherwise, returns UNAUTHORIZED when a user with e-mail provided is already exists
   */
  @Operation(summary = "Create a new user")
  @PostMapping("/register")
  public String register(@RequestBody RegisterRequest request) {
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

    return jwtService.generateToken(request.getEmail(), List.of(UserType.USER));
  }
}
