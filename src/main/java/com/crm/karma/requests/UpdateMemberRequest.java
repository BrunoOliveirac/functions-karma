package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UpdateMemberRequest {
  @Schema(description = "Member identifier")
  private UUID id;

  @Schema(description = "Member's name")
  private String name;

  @Schema(description = "Member's e-mail")
  private String email;
}
