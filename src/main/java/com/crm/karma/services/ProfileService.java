package com.crm.karma.services;

import com.crm.karma.models.User;
import com.crm.karma.repositories.UserRepository;
import com.crm.karma.requests.UpdateProfileRequest;
import com.crm.karma.responses.ProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ProfileService {

  private static final int MAX_AVATAR_LENGTH = 200_000;
  private static final Pattern AVATAR_DATA_URL = Pattern.compile(
    "^data:image/(png|jpe?g|webp);base64,[A-Za-z0-9+/=\\r\\n]+$"
  );

  private final UserService userService;
  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final PasswordService passwordService;
  private final CredentialService credentialService;
  private final TokenBlacklistService tokenBlacklistService;

  public ProfileService(
    UserService userService,
    JwtService jwtService,
    UserRepository userRepository,
    PasswordService passwordService,
    CredentialService credentialService,
    TokenBlacklistService tokenBlacklistService
  ) {
    this.userService = userService;
    this.jwtService = jwtService;
    this.userRepository = userRepository;
    this.passwordService = passwordService;
    this.credentialService = credentialService;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  /**
   * Build the profile response for an authenticated user.
   *
   * @param email Authenticated user e-mail extracted from the JWT
   * @return Profile details including avatar when present
   */
  public ProfileResponse getProfile(String email) {
    return toResponse(requireActiveUser(email), null);
  }

  /**
   * Update the authenticated user's own profile. Avatar can only be changed here.
   * When e-mail or password changes, the current JWT is revoked and a fresh one is returned.
   *
   * @param email Authenticated user e-mail extracted from the JWT
   * @param currentToken Current Bearer JWT to revoke when credentials change
   * @param request Profile fields to update
   * @return Updated profile details (includes token when credentials changed)
   */
  public ProfileResponse updateProfile(
    String email,
    String currentToken,
    UpdateProfileRequest request
  ) {
    User user = requireActiveUser(email);

    if (!StringUtils.hasText(request.getName())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
    }

    String nextEmail = request.getEmail().trim().toLowerCase();
    var existingUser = userRepository.findByEmailAndIdNot(nextEmail, user.getId());

    if (existingUser.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail already exists!");
    }

    boolean emailChanged = !nextEmail.equals(user.getEmail());
    boolean passwordChanged = StringUtils.hasText(request.getPassword());

    user.setName(request.getName().trim());
    user.setEmail(nextEmail);

    if (request.getAvatar() != null) {
      user.setAvatar(normalizeAvatar(request.getAvatar()));
    }

    if (passwordChanged) {
      String hash = passwordService.hashPassword(request.getPassword());
      credentialService.updatePassword(user.getId(), hash);
    }

    User saved = userService.save(user);
    String token = null;

    if (emailChanged || passwordChanged) {
      tokenBlacklistService.revoke(currentToken);
      token = jwtService.generateToken(saved.getEmail(), List.of(saved.getType()));
    }

    return toResponse(saved, token);
  }

  private User requireActiveUser(String email) {
    User user = userService.getByEmail(email);

    if (user == null || !Boolean.TRUE.equals(user.getActive()) || user.getDeletedAt() != null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
    }

    return user;
  }

  private String normalizeAvatar(String avatar) {
    if (!StringUtils.hasText(avatar)) {
      return null;
    }

    String trimmed = avatar.trim();

    if (trimmed.length() > MAX_AVATAR_LENGTH) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar is too large");
    }

    if (!AVATAR_DATA_URL.matcher(trimmed).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid avatar format");
    }

    return trimmed.replaceAll("\\s", "");
  }

  private ProfileResponse toResponse(User user, String token) {
    return new ProfileResponse(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getAvatar(),
      user.getType(),
      token
    );
  }
}
