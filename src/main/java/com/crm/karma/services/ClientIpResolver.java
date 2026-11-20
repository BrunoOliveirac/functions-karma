package com.crm.karma.services;

import com.crm.karma.configs.RateLimitProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Uses {@code X-Forwarded-For} only when the TCP peer is the panel (or another
 * configured proxy). A direct caller on port 8080 is keyed by the connection
 * address, so a forged header cannot rotate the bucket.
 */
@Component
public class ClientIpResolver {

  private final RateLimitProperties properties;
  private final Cache<String, Set<String>> resolvedHosts = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofSeconds(30))
    .maximumSize(64)
    .build();

  public ClientIpResolver(RateLimitProperties properties) {
    this.properties = properties;
  }

  public String resolve(HttpServletRequest request) {
    String remote = request.getRemoteAddr();

    if (isTrustedProxy(remote)) {
      String forwarded = firstForwardedIp(request.getHeader("X-Forwarded-For"));
      if (forwarded != null) return forwarded;
    }

    String canonical = canonicalIp(remote);
    return canonical != null ? canonical : "unknown";
  }

  public boolean isTrustedProxy(String remoteAddr) {
    String remote = canonicalIp(remoteAddr);
    if (remote == null) return false;

    List<String> hosts = new ArrayList<>();

    for (String entry : properties.getTrustedProxies()) {
      if (entry == null || entry.isBlank()) continue;

      String trimmed = entry.trim();
      String literal = canonicalIp(trimmed);
      if (literal != null) {
        if (literal.equals(remote)) return true;
        continue;
      }

      hosts.add(trimmed);
    }

    for (String host : hosts) {
      if (resolvedHosts.get(host, this::resolveHost).contains(remote)) return true;
    }

    return false;
  }

  private Set<String> resolveHost(String host) {
    Set<String> addresses = new LinkedHashSet<>();

    try {
      for (InetAddress address : InetAddress.getAllByName(host)) {
        String canonical = canonicalIp(address.getHostAddress());
        if (canonical != null) addresses.add(canonical);
      }
    } catch (UnknownHostException ignored) {
      // Hostname is not a proxy we can identify right now.
    }

    return addresses;
  }

  private static String firstForwardedIp(String header) {
    if (header == null || header.isBlank()) return null;

    for (String part : header.split(",")) {
      String canonical = canonicalIp(part);
      if (canonical != null) return canonical;
    }

    return null;
  }

  static String canonicalIp(String value) {
    if (value == null) return null;

    String trimmed = value.trim();
    if (trimmed.isEmpty() || trimmed.length() > 64) return null;

    if (trimmed.startsWith("[") && trimmed.endsWith("]") && trimmed.length() > 2) {
      trimmed = trimmed.substring(1, trimmed.length() - 1);
    }

    if (!isIpLiteral(trimmed)) return null;

    try {
      return InetAddress.getByName(trimmed).getHostAddress();
    } catch (UnknownHostException exception) {
      return null;
    }
  }

  private static boolean isIpLiteral(String value) {
    if (value.indexOf(':') >= 0) {
      return value.chars().allMatch(ch ->
        (ch >= '0' && ch <= '9')
          || (ch >= 'a' && ch <= 'f')
          || (ch >= 'A' && ch <= 'F')
          || ch == ':'
          || ch == '.'
      );
    }

    String[] parts = value.split("\\.", -1);
    if (parts.length != 4) return false;

    for (String part : parts) {
      if (part.isEmpty() || part.length() > 3) return false;

      int octet;
      try {
        octet = Integer.parseInt(part);
      } catch (NumberFormatException exception) {
        return false;
      }

      if (octet < 0 || octet > 255) return false;
    }

    return true;
  }
}
