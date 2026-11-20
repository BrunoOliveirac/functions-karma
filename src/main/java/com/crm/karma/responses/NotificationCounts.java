package com.crm.karma.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Notification totals for the current search, ignoring status")
public record NotificationCounts(
  @Schema(description = "Total non-deleted notifications matching the search")
  long all,
  @Schema(description = "Unread notifications matching the search")
  long unread,
  @Schema(description = "Read notifications matching the search")
  long read
) {
}
