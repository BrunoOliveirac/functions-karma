package com.crm.karma.controllers;

import com.crm.karma.models.User;
import com.crm.karma.requests.CheckSupportEmailRequest;
import com.crm.karma.requests.UpdateSupportPasswordRequest;
import com.crm.karma.requests.UpsertSupportRequest;
import com.crm.karma.responses.StatusResponse;
import com.crm.karma.services.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/supports")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Supports")
public class SupportController {

  private final SupportService supportService;

  public SupportController(SupportService supportService) {
    this.supportService = supportService;
  }

  /**
   * List all support users
   *
   * @return A support list or an empty list
   */
  @Operation(summary = "List all supports")
  @GetMapping("/list")
  public List<User> listSupports() {
    return supportService.getAll();
  }

  /**
   * Create or update a support user
   *
   * @param request The support data to be created or updated
   * @return Support ID created or updated
   */
  @Operation(summary = "Create or edit a support user")
  @PostMapping("/upsert")
  public UUID upsertSupport(@RequestBody UpsertSupportRequest request) {
    return supportService.upsert(request);
  }

  /**
   * Verify if the e-mail is already registered
   *
   * @param request Object with support ID and e-mail
   * @return True when the e-mail is available
   */
  @Operation(summary = "Verify the availability of an e-mail address")
  @PostMapping("/check-email")
  public Boolean checkEmail(@RequestBody CheckSupportEmailRequest request) {
    return supportService.checkEmail(request);
  }

  /**
   * Toggle the active status of a support user
   *
   * @param supportId Support ID
   * @return OK status when toggle is successful
   */
  @Operation(summary = "Activate or deactivate a support user")
  @PatchMapping("/toggle-status/{supportId}")
  public StatusResponse toggleStatus(@PathVariable UUID supportId) {
    supportService.toggleStatus(supportId);
    return new StatusResponse("OK");
  }

  /**
   * Update the password of a support user
   *
   * @param supportId Support ID
   * @param request   Object with the new password
   * @return OK status when update is successful
   */
  @Operation(summary = "Update a support user's password")
  @PatchMapping("/update-password/{supportId}")
  public StatusResponse updatePassword(
    @PathVariable UUID supportId,
    @RequestBody UpdateSupportPasswordRequest request
  ) {
    supportService.updatePassword(supportId, request.getPassword());
    return new StatusResponse("OK");
  }

  /**
   * Soft delete a support user
   *
   * @param supportId Support ID to be deleted
   * @return OK status when deletion is successful
   */
  @Operation(summary = "Delete logically a support user")
  @DeleteMapping("/delete/{supportId}")
  public StatusResponse deleteSupport(@PathVariable UUID supportId) {
    supportService.delete(supportId);
    return new StatusResponse("OK");
  }
}
