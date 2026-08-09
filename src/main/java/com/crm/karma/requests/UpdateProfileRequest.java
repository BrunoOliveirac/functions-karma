package com.crm.karma.requests;

import com.crm.karma.configs.PassthroughStringDeserializer;
import com.crm.karma.validations.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import tools.jackson.databind.annotation.JsonDeserialize;

@Getter
public class UpdateProfileRequest {
  @Schema(description = "User's name")
  private String name;

  @ValidEmail
  @Schema(description = "User's e-mail")
  private String email;

  @Schema(description = "Optional new password. Omit or leave blank to keep the current password.")
  private String password;

  @JsonDeserialize(using = PassthroughStringDeserializer.class)
  @Schema(
    description = "Avatar as a data URL (data:image/...;base64,...). "
      + "Send an empty string to remove the avatar. Omit to keep the current avatar."
  )
  private String avatar;
}
