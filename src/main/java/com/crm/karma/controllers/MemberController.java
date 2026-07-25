package com.crm.karma.controllers;

import com.crm.karma.models.User;
import com.crm.karma.repositories.UserRepository;
import com.crm.karma.requests.*;
import com.crm.karma.responses.PaginatedResponse;
import com.crm.karma.responses.StatusResponse;
import com.crm.karma.services.MemberService;
import com.crm.karma.services.ProjectMemberService;
import com.crm.karma.services.UserMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/members")
@PreAuthorize("hasRole('USER')")
@Tag(name = "Members")
public class MemberController {
  private final UserRepository userRepository;

  private final MemberService memberService;
  private final UserMemberService userMemberService;
  private final ProjectMemberService projectMemberService;

  public MemberController(
    MemberService memberService,
    UserRepository userRepository,
    UserMemberService userMemberService,
    ProjectMemberService projectMemberService
  ) {
    this.memberService = memberService;
    this.userRepository = userRepository;
    this.userMemberService = userMemberService;
    this.projectMemberService = projectMemberService;
  }

  /**
   * List all member users
   *
   * @return A member list or an empty list
   */
  @Operation(summary = "List all members")
  @GetMapping("/list")
  public List<User> listMembers() {
    return memberService.getAll();
  }

  /**
   * Get all linked members to the logged user with pagination
   *
   * @param userId Logged user ID
   * @param paginatedQueryRequest Object with page and query
   * @return An object with totalPages and the members
   */
  @Operation(summary = "List members linked to a user")
  @GetMapping("/list/{userId}")
  public PaginatedResponse<User> listLinkedMembers(
    @PathVariable UUID userId,
    PaginatedQueryRequest paginatedQueryRequest
  ) {
    Pageable pageable = PageRequest.of(
      paginatedQueryRequest.page() - 1,
      10
    );

    Page<@NonNull User> paginatedMembers = userMemberService.getAllLinkedMembers(
      paginatedQueryRequest.query(),
      userId,
      pageable
    );

    return new PaginatedResponse<>(
      paginatedMembers.getContent(),
      paginatedMembers.getTotalPages()
    );
  }

  /**
   * Get All project IDS linked to a member
   *
   * @param memberId Member ID
   * @return List of project IDs
   */
  @Operation(summary = "Get all project IDs linked to a member")
  @GetMapping("/projects/{memberId}")
  public List<UUID> getLinkedProjectIds(@PathVariable UUID memberId) {
    return projectMemberService.findByMemberId(memberId);
  }

  /**
   * Create a member user
   *
   * @param request The member data to be created
   */
  @Operation(summary = "Create a member user")
  @PostMapping("/create")
  public void createMember(@RequestBody CreateMemberRequest request) {
    memberService.add(request);
  }

  /**
   * Update a member user
   *
   * @param request The member data to be updated
   */
  @Operation(summary = "Update a member user")
  @PostMapping("/update/{memberId}")
  public void updateMember(
    @PathVariable UUID memberId,
    @RequestBody UpdateMemberRequest request
  ) {
    memberService.update(request);
  }

  /**
   * Create or update a member user
   *
   * @param request The member data to be created or updated
   * @return Member ID created or updated
   */
  @Operation(summary = "Update a member user")
  @PostMapping("/management-projects/{memberId}")
  public StatusResponse managementProjectMembers(
    @PathVariable UUID memberId,
    @RequestBody ManagementProjectMembersRequest request
  ) {
    projectMemberService.managementProjectMembers(
      memberId,
      request.getProjectIds(),
      request.getInitialProjectIds()
    );

    return new StatusResponse("success");
  }

  /**
   * Link a member with a user
   *
   * @param linkMemberRequest Member's e-mail and logged user ID
   * @return "Success" message
   */
  @Operation(summary = "Link a member with a user")
  @PostMapping("/link")
  public StatusResponse linkMember(@RequestBody LinkMemberRequest linkMemberRequest) {
    User member = userRepository
      .findByEmail(linkMemberRequest.getEmail())
      .orElseThrow(() -> new RuntimeException("Member not found!"));

    User user = userRepository
      .findById(linkMemberRequest.getUserId())
      .orElseThrow(() -> new RuntimeException("User not found!"));

    userMemberService.add(member, user);
    return new StatusResponse("success");
  }

  /**
   * Unlink a member from a user
   *
   * @param request Member ID and logged user ID
   * @return "Success" message
   */
  @Operation(summary = "Unlink a member from a user")
  @DeleteMapping("/unlink")
  public StatusResponse unlinkMember(@RequestBody UnlinkMemberRequest request) {
    userMemberService.unlink(request.getMemberId(), request.getUserId());
    return new StatusResponse("success");
  }

  /**
   * Verify if the e-mail is already registered
   *
   * @param request Object with member ID and e-mail
   * @return True when the e-mail is available
   */
  @Operation(summary = "Verify the availability of an e-mail address")
  @PostMapping("/check-email")
  public StatusResponse checkEmail(@RequestBody CheckMemberEmailRequest request) {
    return memberService.checkEmail(request);
  }

  /**
   * Update the password of a member user
   *
   * @param memberId Member ID
   * @param request  Object with the new password
   * @return OK status when update is successful
   */
  @Operation(summary = "Update a member user's password")
  @PatchMapping("/update-password/{memberId}")
  public StatusResponse updatePassword(
    @PathVariable UUID memberId,
    @RequestBody UpdateMemberPasswordRequest request
  ) {
    memberService.updatePassword(memberId, request.getPassword());
    return new StatusResponse("OK");
  }

  /**
   * Soft delete a member user
   *
   * @param memberId Member ID to be deleted
   * @return OK status when deletion is successful
   */
  @Operation(summary = "Delete logically a member user")
  @DeleteMapping("/delete/{memberId}")
  public StatusResponse deleteMember(@PathVariable UUID memberId) {
    memberService.delete(memberId);
    return new StatusResponse("OK");
  }
}
