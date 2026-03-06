package com.crm.kizuna.services;

import com.crm.kizuna.responses.AuthResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.expiration}")
  private Integer jwtExpiration;

  Key getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  public AuthResponse generateToken(String email) {
    Date now = new Date();
    Date expirationDate = new Date(now.getTime() + jwtExpiration);

    return new AuthResponse(expirationDate,
      Jwts.builder()
        .subject(email)
        .issuedAt(now)
        .expiration(expirationDate)
        .signWith(getSigningKey())
        .compact()
    );
  }
}
