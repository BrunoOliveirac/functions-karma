package com.crm.karma.requests;

import com.crm.karma.validations.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RegisterRequest {
  @Schema(description = "User's name")
  private String name;

  @ValidEmail
  @Schema(description = "User's e-mail")
  private String email;

  @Schema(description = "User's password")
  private String password;
}
