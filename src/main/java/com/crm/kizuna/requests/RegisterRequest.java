package com.crm.kizuna.requests;

import lombok.Getter;

@Getter
public class RegisterRequest {
  private String name;
  private String email;
  private String password;
}
