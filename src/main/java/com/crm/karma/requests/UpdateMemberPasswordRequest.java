package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UpdateMemberPasswordRequest {
  @Schema(description = "Member's new password")
  private String password;
}
