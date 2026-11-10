package com.crm.karma.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.OptionalLong;

@Service
public class RateLimitService {

  private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
    .expireAfterAccess(Duration.ofHours(2))
    .maximumSize(100_000)
    .build();

  /**
   * @return seconds until the next token, or empty when the request is allowed
   */
  public OptionalLong tryConsume(String key, int capacity, Duration period) {
    ConsumptionProbe probe = bucket(key, capacity, period).tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) return OptionalLong.empty();

    long seconds = Math.max(
      1,
      (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L
    );
    return OptionalLong.of(seconds);
  }

  private Bucket bucket(String key, int capacity, Duration period) {
    return buckets.get(key, ignored -> Bucket.builder()
      .addLimit(Bandwidth.builder()
        .capacity(capacity)
        .refillGreedy(capacity, period)
        .build())
      .build());
  }
}
