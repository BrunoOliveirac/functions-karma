package com.crm.karma.requests;

import com.crm.karma.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

public record ListTicketsQueryRequest(
  @Schema(description = "1-based page number")
  int page,
  @Schema(description = "Tickets per page, default is 5")
  int pageSize,
  @Schema(description = "Optional search on title, description, and ticket code")
  String query,
  @Schema(description = "Optional status filter")
  TicketStatus status,
  @Schema(description = "Optional creation date start, inclusive (yyyy-MM-dd, UTC)")
  LocalDate startDate,
  @Schema(description = "Optional creation date end, inclusive (yyyy-MM-dd, UTC)")
  LocalDate endDate,
  @Schema(description = "Owner user id. Users and members pass their own id")
  UUID userId,
  @Schema(description = "Support id. Support passes their own id")
  UUID supportId
) {
}
