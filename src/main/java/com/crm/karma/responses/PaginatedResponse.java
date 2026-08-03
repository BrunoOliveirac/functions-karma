package com.crm.karma.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PaginatedResponse<T> {

  @Schema(description = "Paginated items")
  private List<T> items;

  @Schema(description = "Total pages to be shown")
  private int totalPages;

  public PaginatedResponse(List<T> items, int totalPages) {
    this.items = items;
    this.totalPages = totalPages;
  }
}
