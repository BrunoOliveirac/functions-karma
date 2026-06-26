package com.crm.karma.services;

import com.crm.karma.enums.UserType;
import com.crm.karma.models.Credential;
import com.crm.karma.models.User;
import com.crm.karma.repositories.UserRepository;
import com.crm.karma.requests.CheckSupportEmailRequest;
import com.crm.karma.requests.UpsertSupportRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SupportService {

  private final UserRepository userRepository;
  private final UserService userService;
  private final PasswordService passwordService;
  private final CredentialService credentialService;

  public SupportService(
    UserRepository userRepository,
    UserService userService,
    PasswordService passwordService,
    CredentialService credentialService
  ) {
    this.userRepository = userRepository;
    this.userService = userService;
    this.passwordService = passwordService;
    this.credentialService = credentialService;
  }

  public List<User> getAll() {
    return userRepository.findAllByTypeAndDeletedAtIsNullOrderByNameAsc(UserType.SUPPORT);
  }

  public User getById(UUID supportId) {
    return userRepository
      .findByIdAndTypeAndDeletedAtIsNull(supportId, UserType.SUPPORT)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support not found!"));
  }

  public Boolean checkEmail(CheckSupportEmailRequest request) {
    var existingUser = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail());

    return existingUser
      .map(user -> request.getSupportId() != null && user.getId().equals(request.getSupportId()))
      .orElse(true);
  }

  public UUID upsert(UpsertSupportRequest request) {
    if (request.getId() == null) return create(request);

    return update(request);
  }

  private UUID create(UpsertSupportRequest request) {
    if (request.getPassword() == null || request.getPassword().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required!");
    }

    if (!checkEmail(new CheckSupportEmailRequest(null, request.getEmail()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail already exists!");
    }

    User support = User.builder()
      .name(request.getName())
      .email(request.getEmail().trim().toLowerCase())
      .type(UserType.SUPPORT)
      .active(request.getActive() != null ? request.getActive() : true)
      .build();

    User createdSupport = userService.add(support);
    String hash = passwordService.hashPassword(request.getPassword());
    credentialService.add(hash, createdSupport.getId());

    return createdSupport.getId();
  }

  private UUID update(UpsertSupportRequest request) {
    User support = getById(request.getId());

    if (!checkEmail(new CheckSupportEmailRequest(request.getId(), request.getEmail()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail already exists!");
    }

    support.setName(request.getName());
    support.setEmail(request.getEmail().trim().toLowerCase());

    if (request.getActive() != null) support.setActive(request.getActive());

    return userService.save(support).getId();
  }

  public void toggleStatus(UUID supportId) {
    User support = getById(supportId);
    support.toggleStatus();
    userService.save(support);
  }

  public void updatePassword(UUID supportId, String password) {
    getById(supportId);
    String hash = passwordService.hashPassword(password);
    credentialService.updatePassword(supportId, hash);
  }

  public void delete(UUID supportId) {
    User support = getById(supportId);
    support.setDeletedAt(Instant.now());
    support.setEmail(UUID.randomUUID() + "@deleted.internal");

    Credential credential = credentialService.getByUserId(supportId);

    if (credential == null)
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Credential not found!");

    credentialService.delete(credential.getId());
    userService.save(support);
  }
}
