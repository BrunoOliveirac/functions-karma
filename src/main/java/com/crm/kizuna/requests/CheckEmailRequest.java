package com.crm.kizuna.requests;

import lombok.Getter;

import java.util.UUID;

@Getter
public class CheckEmailRequest {
  private UUID userId;
  private String email;
}
