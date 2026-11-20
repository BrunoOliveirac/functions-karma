package com.crm.karma.responses;

import com.crm.karma.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Logged-in user profile details")
public record ProfileResponse(
  @Schema(description = "User identifier")
  UUID id,
  @Schema(description = "User's name")
  String name,
  @Schema(description = "User's e-mail")
  String email,
  @Schema(description = "User's avatar as a data URL")
  String avatar,
  @Schema(description = "User's type")
  UserType type,
  @Schema(
    description = "Fresh JWT when e-mail or password changed; null otherwise. "
      + "Clients must replace the previous session token with this value."
  )
  String token
) {
}
