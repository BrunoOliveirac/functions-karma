package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UpsertSupportRequest {
  @Schema(description = "Support identifier")
  private UUID id;

  @Schema(description = "Support's name")
  private String name;

  @Schema(description = "Support's e-mail")
  private String email;

  @Schema(description = "Support's password (required on create)")
  private String password;

  @Schema(description = "Active status of the support")
  private Boolean active;
}
