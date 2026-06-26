package com.crm.karma.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CheckSupportEmailRequest {
  @Schema(description = "Support identifier to exclude when editing")
  private UUID supportId;

  @Schema(description = "Support e-mail")
  private String email;
}
