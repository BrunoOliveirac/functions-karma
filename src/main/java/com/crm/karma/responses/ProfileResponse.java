package com.crm.karma.responses;

import com.crm.karma.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Logged-in user profile details")
public record ProfileResponse(
  UUID id,
  String name,
  String email,
  String avatar,
  UserType type,
  @Schema(
    description = "Fresh JWT when e-mail or password changed; null otherwise. "
      + "Clients must replace the previous session token with this value."
  )
  String token
) {
}
