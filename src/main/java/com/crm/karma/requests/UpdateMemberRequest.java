package com.crm.karma.requests;

import com.crm.karma.validations.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UpdateMemberRequest {
  @Schema(description = "Member identifier")
  private UUID id;

  @Schema(description = "Member's name")
  private String name;

  @ValidEmail
  @Schema(description = "Member's e-mail")
  private String email;
}
