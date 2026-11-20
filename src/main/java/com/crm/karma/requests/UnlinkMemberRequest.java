package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UnlinkMemberRequest {
  @Schema(description = "Member ID")
  UUID memberId;

  @Schema(description = "Logged user id")
  UUID userId;
}
