package com.crm.karma.controllers;

import com.crm.karma.models.Notification;
import com.crm.karma.repositories.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@PreAuthorize("hasAnyRole('MEMBER', 'USER', 'SUPPORT')")
@Tag(name = "Notification")
public class NotificationController {

  private final NotificationRepository notificationRepository;

  public NotificationController(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  /**
   * List all logged user notifications
   *
   * @param userId Logged user ID
   * @return An array of notifications
   */
  @Operation(summary = "List all notifications of a user")
  @GetMapping("latest/{userId}")
  public List<Notification> listNotifications(@PathVariable UUID userId) {
    Pageable pageable = PageRequest.of(0, 3);

    return notificationRepository
      .findTop5ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
        userId,
        pageable
      );
  }
}
