package com.crm.karma.responses;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ticket totals for the home cards. all is the sum of the four statuses")
public record TicketCounts(
  @Schema(description = "Open tickets. User and member: that user's. Support: every open ticket")
  long open,
  @Schema(description = "In-progress tickets. User and member: that user's. Support: assigned to that support")
  long inProgress,
  @Schema(description = "Resolved tickets. User and member: that user's. Support: assigned to that support")
  long resolved,
  @Schema(description = "Cancelled tickets. User and member: that user's. Support: assigned to that support")
  long cancelled,
  @Schema(description = "Sum of open, in progress, resolved, and cancelled")
  long all
) {
  public TicketCounts(long open, long inProgress, long resolved, long cancelled) {
    this(open, inProgress, resolved, cancelled, open + inProgress + resolved + cancelled);
  }

  public static TicketCounts empty() {
    return new TicketCounts(0, 0, 0, 0);
  }
}
