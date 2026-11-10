package com.crm.karma.services;

import com.crm.karma.models.User;
import com.crm.karma.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceLoginLockTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  @Test
  void fifthFailureLocksTheAccountForFiveMinutes() {
    User user = User.builder().failedLoginAttempts(4).build();
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Instant before = Instant.now();
    assertTrue(userService.registerFailedLogin(user));

    assertEquals(UserService.MAX_LOGIN_ATTEMPTS, user.getFailedLoginAttempts());
    assertNotNull(user.getLockedUntil());
    assertFalse(user.getLockedUntil().isBefore(
      before.plus(UserService.LOGIN_LOCK_DURATION).minusSeconds(2)
    ));
    assertFalse(user.getLockedUntil().isAfter(
      Instant.now().plus(UserService.LOGIN_LOCK_DURATION).plusSeconds(2)
    ));
    assertTrue(userService.isLoginLocked(user));
  }
}
