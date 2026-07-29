package com.crm.karma.responses;

import com.crm.karma.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Logged-in user profile details")
public record ProfileResponse(
  UUID id,
  String name,
  String email,
  UserType type
) {
}
