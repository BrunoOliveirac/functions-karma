package com.crm.karma.services;

import com.crm.karma.models.User;
import com.crm.karma.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class UserService {
  public static final int MAX_LOGIN_ATTEMPTS = 5;
  public static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(1);

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User getByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
  }

  public User add(User user) {
    user.setActive(true);
    return userRepository.save(user);
  }

  public User save(User user) {
    return userRepository.save(user);
  }

  public boolean isLoginLocked(User user) {
    Instant lockedUntil = user.getLockedUntil();
    return lockedUntil != null && lockedUntil.isAfter(Instant.now());
  }

  /**
   * Clears an expired lock so the user can try again with a fresh attempt counter.
   */
  public void clearExpiredLoginLock(User user) {
    Instant lockedUntil = user.getLockedUntil();
    if (lockedUntil != null && !lockedUntil.isAfter(Instant.now())) {
      user.setLockedUntil(null);
      user.setFailedLoginAttempts(0);
      userRepository.save(user);
    }
  }

  /**
   * Increments failed attempts and locks the account when the limit is reached.
   *
   * @return {@code true} when the account became (or already is) locked
   */
  public boolean registerFailedLogin(User user) {
    int attempts = Optional.ofNullable(user.getFailedLoginAttempts()).orElse(0) + 1;
    user.setFailedLoginAttempts(attempts);

    if (attempts >= MAX_LOGIN_ATTEMPTS) {
      user.setLockedUntil(Instant.now().plus(LOGIN_LOCK_DURATION));
      userRepository.save(user);
      return true;
    }

    userRepository.save(user);
    return false;
  }

  public void resetLoginAttempts(User user) {
    boolean needsReset =
      (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0)
        || user.getLockedUntil() != null;

    if (!needsReset) return;
    

    user.setFailedLoginAttempts(0);
    user.setLockedUntil(null);
    userRepository.save(user);
  }
}
