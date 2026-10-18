package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record AcceptTicketRequest(
  @Schema(description = "Ticket identifier")
  UUID ticketId,
  @Schema(description = "Support identifier")
  UUID supportId
) {
}
