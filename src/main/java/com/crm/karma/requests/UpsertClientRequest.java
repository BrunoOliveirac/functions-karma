package com.crm.karma.requests;

import com.crm.karma.configs.PassthroughStringDeserializer;
import com.crm.karma.validations.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.UUID;

@Getter
public class UpsertClientRequest {

  @Schema(description = "Client ID")
  private UUID id;

  @Schema(description = "Client's name")
  private String name;

  @ValidEmail
  @Schema(description = "Client's e-mail address")
  private String email;

  @Schema(description = "Client's phone")
  private String phone;

  @Schema(description = "Notes (description or observation) about the client")
  @Size(max = 500, message = "Notes should have a maximum of 500 characters")
  private String notes;

  @Schema(description = "ID of user who created it")
  private UUID userId;

  @Schema(description = "Client's budget")
  private Integer budget;

  @Schema(description = "ID sector linked to Client")
  private UUID sectorId;

  @JsonDeserialize(using = PassthroughStringDeserializer.class)
  @Schema(description = "Cover as a data URL (data:image/...;base64,...). " + "Send an empty string to remove the cover. Omit to keep the current avatar.")
  private String cover;

  @JsonDeserialize(using = PassthroughStringDeserializer.class)
  @Schema(description = "Avatar as a data URL (data:image/...;base64,...). " + "Send an empty string to remove the avatar. Omit to keep the current avatar.")
  private String avatar;
}
