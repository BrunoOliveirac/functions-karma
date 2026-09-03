package com.crm.karma.responses;

import com.crm.karma.models.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated notifications of a user")
public record NotificationListResponse(
  @Schema(description = "Notifications for the requested page")
  List<Notification> notifications,
  @Schema(description = "Whether another page is available")
  boolean hasMore,
  @Schema(description = "Tab counts for the current search")
  NotificationCounts counts
) {
}
