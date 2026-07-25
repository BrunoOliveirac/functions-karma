package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
public class LinkMemberRequest {
  @Schema(description = "Member's e-mail")
  String email;

  @Schema(description = "Logged user id")
  UUID userId;
}
