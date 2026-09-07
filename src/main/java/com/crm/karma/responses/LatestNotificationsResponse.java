package com.crm.karma.responses;

import com.crm.karma.models.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Latest notifications of a user with unread flag")
public record LatestNotificationsResponse(
  @Schema(description = "The 3 most recent notifications")
  List<Notification> notifications,
  @Schema(description = "Whether the user has any unread notification")
  boolean hasUnreadNotifications
) {
}
