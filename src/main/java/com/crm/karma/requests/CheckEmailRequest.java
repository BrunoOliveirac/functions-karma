package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CheckEmailRequest {
  @Schema(description = "User identifier")
  private UUID userId;

  @Schema(description = "User's e-mail")
  private String email;
}
