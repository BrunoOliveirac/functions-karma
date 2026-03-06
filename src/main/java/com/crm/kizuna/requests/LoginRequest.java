package com.crm.kizuna.requests;

import lombok.Getter;

@Getter
public class LoginRequest {
  private String email;
  private String password;
}
