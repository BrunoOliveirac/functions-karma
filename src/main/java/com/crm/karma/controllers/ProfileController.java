package com.crm.karma.controllers;

import com.crm.karma.models.User;
import com.crm.karma.responses.ProfileResponse;
import com.crm.karma.services.JwtService;
import com.crm.karma.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/profile")
@PreAuthorize("hasAnyRole('ADMIN', 'MEMBER', 'USER', 'SUPPORT')")
@Tag(name = "Profile")
public class ProfileController {

  private final JwtService jwtService;
  private final UserService userService;

  public ProfileController(JwtService jwtService, UserService userService) {
    this.jwtService = jwtService;
    this.userService = userService;
  }

  /**
   * Get the logged-in user details from the JWT token
   *
   * @param authHeader Authorization header with the Bearer token
   * @return The logged-in user profile (id, name, email, type)
   */
  @Operation(summary = "Get logged-in user details")
  @GetMapping
  public ProfileResponse getProfile(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
    }

    String token = authHeader.substring(7);
    String email = jwtService.extractEmail(token);
    User user = userService.getByEmail(email);

    if (user == null || !user.getActive()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
    }

    return new ProfileResponse(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getType()
    );
  }
}
