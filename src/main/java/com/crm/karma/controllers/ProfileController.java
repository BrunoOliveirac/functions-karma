package com.crm.karma.controllers;

import com.crm.karma.requests.UpdateProfileRequest;
import com.crm.karma.responses.ProfileResponse;
import com.crm.karma.services.JwtService;
import com.crm.karma.services.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  private final ProfileService profileService;

  public ProfileController(JwtService jwtService, ProfileService profileService) {
    this.jwtService = jwtService;
    this.profileService = profileService;
  }

  /**
   * Get the logged-in user details from the JWT token
   *
   * @param authHeader Authorization header with the Bearer token
   * @return The logged-in user profile (id, name, email, avatar, type)
   */
  @Operation(summary = "Get logged-in user details")
  @GetMapping
  public ProfileResponse getProfile(@RequestHeader("Authorization") String authHeader) {
    return profileService.getProfile(extractEmail(authHeader));
  }

  /**
   * Update the logged-in user's own profile, including avatar and optional password.
   * When e-mail or password changes, the current token is revoked and a new one is returned.
   *
   * @param authHeader Authorization header with the Bearer token
   * @param request Profile fields to update
   * @return The updated profile (with a new token when credentials changed)
   */
  @Operation(summary = "Update logged-in user profile")
  @PutMapping
  public ProfileResponse updateProfile(
    @RequestHeader("Authorization") String authHeader,
    @Valid @RequestBody UpdateProfileRequest request
  ) {
    String token = extractToken(authHeader);
    return profileService.updateProfile(jwtService.extractEmail(token), token, request);
  }

  private String extractEmail(String authHeader) {
    return jwtService.extractEmail(extractToken(authHeader));
  }

  private String extractToken(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
    }

    return authHeader.substring(7);
  }
}
