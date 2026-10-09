package com.crm.karma.responses;

import com.crm.karma.enums.TicketStatus;
import com.crm.karma.models.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Ticket visible to the panel")
public record TicketResponse(
  @Schema(description = "Ticket id")
  UUID id,
  @Schema(description = "Ticket title")
  String title,
  @Schema(description = "Ticket description")
  String description,
  @Schema(description = "Support acceptance date")
  Instant acceptedAt,
  @Schema(description = "Ticket status")
  TicketStatus status,
  @Schema(description = "Human-facing ticket code")
  Long code,
  @Schema(description = "User who opened the ticket")
  UUID userId,
  @Schema(description = "Name of the user who opened the ticket")
  String userName,
  @Schema(description = "Avatar of the user who opened the ticket")
  String userAvatar,
  @Schema(description = "Support who accepted the ticket")
  UUID supportId,
  @Schema(description = "Name of the support linked to the ticket")
  String supportName,
  @Schema(description = "Avatar of the support linked to the ticket")
  String supportAvatar,
  @Schema(description = "Active flag")
  Boolean active,
  @Schema(description = "Creation date")
  Instant createdAt,
  @Schema(description = "Last update date")
  Instant updatedAt
) {
  public static TicketResponse from(Ticket ticket) {
    return new TicketResponse(
      ticket.getId(),
      ticket.getTitle(),
      ticket.getDescription(),
      ticket.getAcceptedAt(),
      ticket.getStatus(),
      ticket.getCode(),
      ticket.getUser().getId(),
      ticket.getUser().getName(),
      ticket.getUser().getAvatar(),
      ticket.getSupport() == null ? null : ticket.getSupport().getId(),
      ticket.getSupport() == null ? null : ticket.getSupport().getName(),
      ticket.getSupport() == null ? null : ticket.getSupport().getAvatar(),
      ticket.getActive(),
      ticket.getCreatedAt(),
      ticket.getUpdatedAt()
    );
  }
}
