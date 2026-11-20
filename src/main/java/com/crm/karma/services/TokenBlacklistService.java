package com.crm.karma.services;

import com.crm.karma.models.RevokedToken;
import com.crm.karma.repositories.RevokedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class TokenBlacklistService {

  private final RevokedTokenRepository revokedTokenRepository;
  private final JwtService jwtService;

  public TokenBlacklistService(
    RevokedTokenRepository revokedTokenRepository,
    JwtService jwtService
  ) {
    this.revokedTokenRepository = revokedTokenRepository;
    this.jwtService = jwtService;
  }

  @Transactional
  public void revoke(String token) {
    Instant expiresAt = jwtService.extractExpiration(token).toInstant();
    String tokenHash = hashToken(token);

    if (revokedTokenRepository.existsByTokenHash(tokenHash)) {
      return;
    }

    revokedTokenRepository.save(
      RevokedToken.builder()
        .tokenHash(tokenHash)
        .expiresAt(expiresAt)
        .build()
    );
  }

  public boolean isRevoked(String token) {
    return revokedTokenRepository.existsByTokenHash(hashToken(token));
  }

  /** Cron job to clean old revoked tokens, executed at every day at 00:05 */
  @Scheduled(cron = "0 5 0 * * *")
  @Transactional
  public void cleanupExpiredTokens() {
    revokedTokenRepository.deleteByExpiresAtBefore(Instant.now());
  }

  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
