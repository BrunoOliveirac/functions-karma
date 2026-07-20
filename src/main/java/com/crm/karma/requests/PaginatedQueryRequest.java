package com.crm.karma.requests;

public record PaginatedQueryRequest(
  int page,
  String query
) {
}
