package com.crm.karma.responses;

import com.crm.karma.models.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public class AuthResponse {

  @Schema(description = "Logged user information")
  public User user;
  @Schema(description = "Expiration date of token")
  public Date expirationDate;
  @Schema(description = "Value from a session token")
  public String token;

  public AuthResponse(Date expirationDate, User user, String token) {
    this.user = user;
    this.expirationDate = expirationDate;
    this.token = token;
  }

  public AuthResponse(Date expirationDate, String token) {
    this.expirationDate = expirationDate;
    this.token = token;
  }
}
