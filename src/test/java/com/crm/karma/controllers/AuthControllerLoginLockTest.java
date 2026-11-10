package com.crm.karma.controllers;

import com.crm.karma.filters.RateLimitFilter;
import com.crm.karma.models.User;
import com.crm.karma.requests.LoginRequest;
import com.crm.karma.services.CredentialService;
import com.crm.karma.services.JwtService;
import com.crm.karma.services.PasswordService;
import com.crm.karma.services.TokenBlacklistService;
import com.crm.karma.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerLoginLockTest {

  @Mock
  private JwtService jwtService;

  @Mock
  private UserService userService;

  @Mock
  private PasswordService passwordService;

  @Mock
  private CredentialService credentialService;

  @Mock
  private TokenBlacklistService tokenBlacklistService;

  @InjectMocks
  private AuthController authController;

  @Test
  void lockedAccountKeepsItsOwn429ApartFromTheRequestRateLimit() {
    User user = User.builder().email("user@email.com").active(true).build();
    LoginRequest request = mock(LoginRequest.class);
    when(request.getEmail()).thenReturn("user@email.com");
    when(userService.getByEmail("user@email.com")).thenReturn(user);
    when(userService.isLoginLocked(user)).thenReturn(true);

    ResponseStatusException exception = assertThrows(
      ResponseStatusException.class,
      () -> authController.login(request)
    );

    assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
    assertEquals(AuthController.ACCOUNT_LOCKED_DETAIL, exception.getReason());
    assertNotEquals(RateLimitFilter.RATE_LIMIT_DETAIL, exception.getReason());
  }
}
