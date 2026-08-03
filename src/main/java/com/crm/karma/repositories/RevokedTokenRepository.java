package com.crm.karma.repositories;

import com.crm.karma.models.RevokedToken;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface RevokedTokenRepository extends JpaRepository<@NonNull RevokedToken, @NonNull String> {

  boolean existsByTokenHash(String tokenHash);

  void deleteByExpiresAtBefore(Instant instant);
}
