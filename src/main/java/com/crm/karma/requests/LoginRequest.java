package com.crm.karma.requests;

import lombok.Getter;

@Getter
public class LoginRequest {
  private String email;
  private String password;
}
