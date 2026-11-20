package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;

public record PaginatedQueryRequest(
  @Schema(description = "1-based page number")
  int page,
  @Schema(description = "Search term")
  String query
) {
}
