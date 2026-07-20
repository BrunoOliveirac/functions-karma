package com.crm.karma.controllers;

import com.crm.karma.models.User;
import com.crm.karma.requests.*;
import com.crm.karma.responses.PaginatedResponse;
import com.crm.karma.responses.StatusResponse;
import com.crm.karma.services.MemberService;
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

  private final MemberService memberService;
  private final UserMemberService userMemberService;

  public MemberController(MemberService memberService, UserMemberService userMemberService) {
    this.memberService = memberService;
    this.userMemberService = userMemberService;
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

  // adicionar slug para filtragem

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
   * Create or update a member user
   *
   * @param request The member data to be created or updated
   * @return Member ID created or updated
   */
  @Operation(summary = "Create a member user")
  @PostMapping("/create")
  public UUID createMember(@RequestBody CreateMemberRequest request) {
    return memberService.add(request);
  }

  /**
   * Create or update a member user
   *
   * @param request The member data to be created or updated
   * @return Member ID created or updated
   */
  @Operation(summary = "Update a member user")
  @PostMapping("/update/{memberId}")
  public UUID updateMember(
    @PathVariable UUID memberId,
    @RequestBody UpdateMemberRequest request
  ) {
    return memberService.update(request);
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
