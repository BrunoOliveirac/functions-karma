package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UpsertProjectRequest {

  @Schema(description = "Project ID")
  private UUID id;

  @Schema(description = "Project's name")
  private String name;

  @Schema(description = "ID of user who created it")
  private UUID userId;

  @Schema(description = "ID of client linked to the project")
  private UUID clientId;
}
