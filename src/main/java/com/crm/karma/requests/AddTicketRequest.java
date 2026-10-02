package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record AddTicketRequest(
  @Schema(description = "User identifier")
  UUID userId,
  @Schema(description = "Ticket's title")
  String title,
  @Schema(description = "Ticket's description")
  String description
) {
}
