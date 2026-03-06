package com.crm.kizuna.responses;

import com.crm.kizuna.models.User;

import java.util.Date;

public class AuthResponse {

  public User user;
  public Date expirationDate;
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
