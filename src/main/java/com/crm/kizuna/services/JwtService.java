package com.crm.kizuna.services;

import com.crm.kizuna.enums.UserType;
import com.crm.kizuna.responses.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secretKey;

  Key getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  public AuthResponse generateToken(String email, List<UserType> roles) {
    ZoneId zone = ZoneId.systemDefault();

    Date expirationDate = Date.from(
      LocalDate.now()
        .plusDays(1)
        .atStartOfDay(zone)
        .toInstant()
    );

    List<String> roleNames = roles.stream()
      .map(Enum::name)
      .toList();

    return new AuthResponse(expirationDate,
      Jwts.builder()
        .subject(email)
        .claim("roles", roleNames)
        .issuedAt(new Date())
        .expiration(expirationDate)
        .signWith(getSigningKey())
        .compact()
    );
  }

  public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public List<UserType> extractRoles(String token) {
    List<?> roles = extractClaim(token, claims -> claims.get("roles", List.class));
    return roles.stream().map(Object::toString).map(UserType::valueOf).toList();
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = Jwts.parser()
      .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
      .build()
      .parseSignedClaims(token)
      .getPayload();

    return claimsResolver.apply(claims);
  }
}
