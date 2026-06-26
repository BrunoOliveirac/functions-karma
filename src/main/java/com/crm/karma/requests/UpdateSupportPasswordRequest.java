package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UpdateSupportPasswordRequest {
  @Schema(description = "Support's new password")
  private String password;
}
