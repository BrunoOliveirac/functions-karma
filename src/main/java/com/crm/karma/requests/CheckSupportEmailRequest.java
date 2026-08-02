package com.crm.karma.requests;

import com.crm.karma.validations.ValidEmail;
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

  @ValidEmail
  @Schema(description = "Support e-mail")
  private String email;
}
