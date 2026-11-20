package com.crm.karma.responses;

import com.crm.karma.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket pushed over SSE")
public record TicketStreamEvent(
  @Schema(description = "Ticket after the change")
  TicketResponse ticket,
  @Schema(description = "Status before the change. Null when the ticket was just created")
  TicketStatus previousStatus
) {
}
