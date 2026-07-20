package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckMemberEmailRequest {
  @Schema(description = "Member identifier to exclude when editing")
  private UUID userId;

  @Schema(description = "Member e-mail")
  private String email;
}
