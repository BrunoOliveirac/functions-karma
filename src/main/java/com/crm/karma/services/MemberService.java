package com.crm.karma.services;

import com.crm.karma.enums.UserType;
import com.crm.karma.models.Credential;
import com.crm.karma.models.User;
import com.crm.karma.models.UserMember;
import com.crm.karma.repositories.UserRepository;
import com.crm.karma.requests.CheckMemberEmailRequest;
import com.crm.karma.requests.CreateMemberRequest;
import com.crm.karma.requests.UpdateMemberRequest;
import com.crm.karma.responses.StatusResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MemberService {

  private final UserService userService;
  private final UserRepository userRepository;
  private final PasswordService passwordService;
  private final CredentialService credentialService;
  private final UserMemberService userMemberService;
  private final ProjectMemberService projectMemberService;

  public MemberService(
    UserService userService,
    UserRepository userRepository,
    PasswordService passwordService,
    CredentialService credentialService,
    UserMemberService userMemberService,
    ProjectMemberService projectMemberService
  ) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.passwordService = passwordService;
    this.credentialService = credentialService;
    this.userMemberService = userMemberService;
    this.projectMemberService = projectMemberService;
  }

  public List<User> getAll() {
    return userRepository.findAllByTypeAndDeletedAtIsNullOrderByNameAsc(UserType.MEMBER);
  }

  public User getById(UUID memberId) {
    return userRepository
      .findByIdAndTypeAndDeletedAtIsNull(memberId, UserType.MEMBER)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found!"));
  }

  public StatusResponse checkEmail(CheckMemberEmailRequest request) {
    // Check if there is a user with the provided e-mail
    var existingUser = userRepository.findByEmail(request.getEmail());
    if (existingUser.isEmpty()) return new StatusResponse("available");

    // Check if the found member is already link to the user
    Optional<UserMember> userMember = userMemberService
      .findByMemberIdAndUserId(existingUser.get().getId(), request.getUserId());

    if (userMember.isPresent()) return new StatusResponse("already-linked");

    UserType userType = existingUser.get().getType();
    if (userType == UserType.MEMBER) return new StatusResponse("to-link");

    return new StatusResponse("in-use");
  }

  @Transactional
  public void add(CreateMemberRequest request) {
    StatusResponse statusResponse = checkEmail(
      CheckMemberEmailRequest
        .builder()
        .userId(request.getUserId())
        .email(request.getEmail())
        .build()
    );

    if (!statusResponse.status().equals("available")) return;

    // Get the logged user
    User user = userRepository
      .findById(request.getUserId())
      .orElseThrow(() -> new RuntimeException("User not found!"));

    // Build the member object
    User member = User.builder()
      .name(request.getName())
      .email(request.getEmail().trim().toLowerCase())
      .type(UserType.MEMBER)
      .build();

    // Create the member and his credential
    User createdMember = userService.add(member);
    String hash = passwordService.hashPassword(request.getPassword());
    credentialService.add(hash, createdMember.getId());

    // Create user-member and project-member links
    userMemberService.add(createdMember, user);
    projectMemberService.saveAll(request.getProjectIds(), user, createdMember);
  }

  public void update(UpdateMemberRequest request) {
    var existingUser = userRepository.findByEmailAndIdNot(request.getEmail(), request.getId());

    if (existingUser.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail already exists!");
    }

    User member = getById(request.getId());
    member.setName(request.getName());
    member.setEmail(request.getEmail().trim().toLowerCase());

    userService.save(member);
  }

  public void updatePassword(UUID memberId, String password) {
    getById(memberId);
    String hash = passwordService.hashPassword(password);
    credentialService.updatePassword(memberId, hash);
  }

  public void delete(UUID memberId) {
    User member = getById(memberId);
    member.setDeletedAt(Instant.now());
    member.setEmail(UUID.randomUUID() + "@deleted.internal");

    Credential credential = credentialService.getByUserId(memberId);

    if (credential == null)
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Credential not found!");

    credentialService.delete(credential.getId());
    userService.save(member);
  }
}
