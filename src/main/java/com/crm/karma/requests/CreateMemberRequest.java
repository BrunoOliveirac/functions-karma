package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class CreateMemberRequest {
  @Schema(description = "Member's name")
  private String name;

  @Schema(description = "Member's e-mail")
  private String email;

  @Schema(description = "Member's password (required on create)")
  private String password;

  @Schema(description = "Logged user ID")
  private UUID userId;

  @Schema(description = "Project IDs")
  private List<UUID> projectIds;
}
