package com.crm.karma.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "karma.rate-limit")
public class RateLimitProperties {

  private int loginPerMinute = 10;
  private int registerPerHour = 5;
  private int authenticatedPerMinute = 120;
  private int anonymousPerMinute = 300;
  private List<String> trustedProxies = new ArrayList<>(List.of(
    "panel-karma",
    "127.0.0.1",
    "::1"
  ));

  public int getLoginPerMinute() {
    return loginPerMinute;
  }

  public void setLoginPerMinute(int loginPerMinute) {
    this.loginPerMinute = loginPerMinute;
  }

  public int getRegisterPerHour() {
    return registerPerHour;
  }

  public void setRegisterPerHour(int registerPerHour) {
    this.registerPerHour = registerPerHour;
  }

  public int getAuthenticatedPerMinute() {
    return authenticatedPerMinute;
  }

  public void setAuthenticatedPerMinute(int authenticatedPerMinute) {
    this.authenticatedPerMinute = authenticatedPerMinute;
  }

  public int getAnonymousPerMinute() {
    return anonymousPerMinute;
  }

  public void setAnonymousPerMinute(int anonymousPerMinute) {
    this.anonymousPerMinute = anonymousPerMinute;
  }

  public List<String> getTrustedProxies() {
    return trustedProxies;
  }

  public void setTrustedProxies(List<String> trustedProxies) {
    this.trustedProxies = trustedProxies == null
      ? new ArrayList<>()
      : new ArrayList<>(trustedProxies);
  }
}
