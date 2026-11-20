package com.crm.karma.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated tickets")
public record TicketListResponse(
  @Schema(description = "Tickets for the requested page")
  List<TicketResponse> items,
  @Schema(description = "Total pages for the current filters")
  int totalPages,
  @Schema(description = "Total tickets matching the current filters")
  long total
) {
}
