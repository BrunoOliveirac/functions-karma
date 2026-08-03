package com.crm.karma.requests;

import com.crm.karma.validations.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
public class LinkMemberRequest {
  @ValidEmail
  @Schema(description = "Member's e-mail")
  String email;

  @Schema(description = "Logged user id")
  UUID userId;
}
