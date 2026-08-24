package com.crm.karma.controllers;

import com.crm.karma.models.User;
import com.crm.karma.responses.LatestNotificationsResponse;
import com.crm.karma.responses.StatusResponse;
import com.crm.karma.services.NotificationService;
import com.crm.karma.services.NotificationSseHub;
import com.crm.karma.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@PreAuthorize("hasAnyRole('MEMBER', 'USER', 'SUPPORT')")
@Tag(name = "Notification")
public class NotificationController {

  private final NotificationService notificationService;
  private final NotificationSseHub notificationSseHub;
  private final UserService userService;

  public NotificationController(
    NotificationService notificationService,
    NotificationSseHub notificationSseHub,
    UserService userService
  ) {
    this.notificationService = notificationService;
    this.notificationSseHub = notificationSseHub;
    this.userService = userService;
  }

  /**
   * List the latest notifications of the logged user
   *
   * @param userId Logged user ID
   * @return Latest notifications plus unread flag
   */
  @Operation(summary = "List latest notifications of a user")
  @GetMapping("latest/{userId}")
  public LatestNotificationsResponse listLatestNotifications(@PathVariable UUID userId) {
    return notificationService.listLatest(userId);
  }

  /**
   * Open an SSE stream for the authenticated user.
   *
   * @return Long-lived emitter scoped to the JWT subject
   */
  @Operation(summary = "Subscribe to notification events of the logged user")
  @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamNotifications() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || authentication.getPrincipal() == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    String email = authentication.getPrincipal().toString();
    User user = userService.getByEmail(email);

    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    return notificationSseHub.subscribe(user.getId());
  }

  /**
   * Mark a notification as read
   *
   * @param notificationId Notification's ID
   * @return Success status
   */
  @Operation(summary = "Mark a notification as read")
  @PatchMapping("mark-as-read/{notificationId}")
  public StatusResponse markNotificationAsRead(@PathVariable UUID notificationId) {
    notificationService.markNotificationAsRead(notificationId);
    return new StatusResponse("success");
  }

  /**
   * Mark all notifications as read
   *
   * @param userId Logged user's ID
   * @return Success status
   */
  @Operation(summary = "Mark a notification as read")
  @PatchMapping("mark-all-as-read/{userId}")
  public StatusResponse markAllNotificationAsRead(@PathVariable UUID userId) {
    notificationService.markAllNotificationAsRead(userId);
    return new StatusResponse("success");
  }

  /**
   * Delete a notification
   * @param notificationId Notification's ID
   * @return Success status
   */
  @Operation(summary = "Delete a notification")
  @DeleteMapping("{notificationId}")
  public StatusResponse deleteNotification(@PathVariable UUID notificationId){
    notificationService.delete(notificationId);
    return new StatusResponse("success");
  }
}
