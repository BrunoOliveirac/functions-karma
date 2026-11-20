package com.crm.karma.filters;

import com.crm.karma.configs.RateLimitProperties;
import com.crm.karma.controllers.AuthController;
import com.crm.karma.services.ClientIpResolver;
import com.crm.karma.services.RateLimitService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

  private RateLimitFilter filter;
  private FilterChain chain;

  @BeforeEach
  void setUp() {
    RateLimitProperties properties = new RateLimitProperties();
    properties.setLoginPerMinute(2);
    properties.setRegisterPerHour(2);
    properties.setAuthenticatedPerMinute(2);
    properties.setAnonymousPerMinute(2);
    properties.setTrustedProxies(List.of("127.0.0.1", "::1"));

    filter = new RateLimitFilter(
      properties,
      new ClientIpResolver(properties),
      new RateLimitService()
    );
    chain = mock(FilterChain.class);
  }

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void loginAllowsTheBudgetThenReturnsRetryAfter() throws Exception {
    MockHttpServletResponse first = postLogin("203.0.113.10", null);
    MockHttpServletResponse second = postLogin("203.0.113.10", null);
    MockHttpServletResponse blocked = postLogin("203.0.113.10", null);

    assertEquals(200, first.getStatus());
    assertEquals(200, second.getStatus());
    assertEquals(429, blocked.getStatus());
    assertNotNull(blocked.getHeader("Retry-After"));
    assertTrue(Integer.parseInt(blocked.getHeader("Retry-After")) >= 1);
    assertTrue(blocked.getContentAsString().contains("\"code\":\"RATE_LIMIT\""));
    assertTrue(blocked.getContentAsString().contains(RateLimitFilter.RATE_LIMIT_DETAIL));
    assertFalse(blocked.getContentAsString().contains("locked"));
    verify(chain, times(2)).doFilter(
      org.mockito.ArgumentMatchers.any(),
      org.mockito.ArgumentMatchers.any()
    );
  }

  @Test
  void forwardedIpIsTheKeyOnlyWhenThePeerIsTheProxy() throws Exception {
    assertEquals(200, postLogin("127.0.0.1", "198.51.100.10").getStatus());
    assertEquals(200, postLogin("127.0.0.1", "198.51.100.10").getStatus());
    assertEquals(429, postLogin("127.0.0.1", "198.51.100.10").getStatus());

    assertEquals(200, postLogin("127.0.0.1", "198.51.100.11").getStatus());

    assertEquals(200, postLogin("203.0.113.8", "198.51.100.21").getStatus());
    assertEquals(200, postLogin("203.0.113.8", "198.51.100.22").getStatus());
    assertEquals(429, postLogin("203.0.113.8", "198.51.100.23").getStatus());
    assertEquals(200, postLogin("203.0.113.9", "198.51.100.21").getStatus());
  }

  @Test
  void ipv6LoopbackCountsAsTheTrustedProxy() throws Exception {
    assertEquals(200, postLogin("0:0:0:0:0:0:0:1", "198.51.100.50").getStatus());
    assertEquals(200, postLogin("0:0:0:0:0:0:0:1", "198.51.100.50").getStatus());
    assertEquals(429, postLogin("127.0.0.1", "198.51.100.50").getStatus());
  }

  @Test
  void invalidForwardedHeaderFallsBackToTheConnectionAddress() throws Exception {
    assertEquals(200, postLogin("127.0.0.1", "not-an-ip").getStatus());
    assertEquals(200, postLogin("127.0.0.1", "also-not-an-ip").getStatus());
    assertEquals(429, postLogin("127.0.0.1", "still-not-an-ip").getStatus());
    assertEquals(200, postLogin("127.0.0.1", "198.51.100.77").getStatus());
  }

  @Test
  void registerLimitIsSeparateFromLogin() throws Exception {
    assertEquals(200, postLogin("203.0.113.40", null).getStatus());
    assertEquals(200, postLogin("203.0.113.40", null).getStatus());
    assertEquals(429, postLogin("203.0.113.40", null).getStatus());

    assertEquals(200, post("/auth/register", "203.0.113.40", null).getStatus());
    assertEquals(200, post("/auth/register", "203.0.113.40", null).getStatus());
    assertEquals(429, post("/auth/register", "203.0.113.40", null).getStatus());
  }

  @Test
  void authenticatedTrafficIsLimitedPerUser() throws Exception {
    authenticate("user-a@email.com");
    assertEquals(200, get("/clients", "203.0.113.50").getStatus());
    assertEquals(200, get("/clients", "203.0.113.50").getStatus());
    assertEquals(429, get("/clients", "198.51.100.50").getStatus());

    authenticate("user-b@email.com");
    assertEquals(200, get("/clients", "203.0.113.50").getStatus());
  }

  @Test
  void anonymousTrafficIsLimitedPerIp() throws Exception {
    assertEquals(200, get("/clients", "203.0.113.60").getStatus());
    assertEquals(200, get("/clients", "203.0.113.60").getStatus());
    assertEquals(429, get("/clients", "203.0.113.60").getStatus());
    assertEquals(200, get("/clients", "203.0.113.61").getStatus());
  }

  @Test
  void swaggerErrorAndAsyncDispatchesAreNotCounted() throws Exception {
    for (int attempt = 0; attempt < 5; attempt++) {
      assertEquals(200, get("/swagger-ui/index.html", "203.0.113.70").getStatus());
      assertEquals(200, get("/v3/api-docs", "203.0.113.70").getStatus());
      assertEquals(200, get("/error", "203.0.113.70").getStatus());
    }

    MockHttpServletRequest async = new MockHttpServletRequest("GET", "/notifications/stream");
    async.setRemoteAddr("203.0.113.71");
    async.setDispatcherType(DispatcherType.ASYNC);

    for (int attempt = 0; attempt < 5; attempt++) {
      MockHttpServletResponse response = new MockHttpServletResponse();
      filter.doFilter(async, response, chain);
      assertEquals(200, response.getStatus());
    }

    assertEquals(200, get("/notifications/stream", "203.0.113.71").getStatus());
  }

  @Test
  void rateLimitBodyIsDistinctFromTheAccountLockMessage() {
    assertNotEquals(AuthController.ACCOUNT_LOCKED_DETAIL, RateLimitFilter.RATE_LIMIT_DETAIL);
    assertFalse(RateLimitFilter.RATE_LIMIT_DETAIL.toLowerCase().contains("locked"));
  }

  private MockHttpServletResponse postLogin(String remote, String forwarded)
    throws ServletException, IOException {
    return post("/auth/login", remote, forwarded);
  }

  private MockHttpServletResponse post(String path, String remote, String forwarded)
    throws ServletException, IOException {
    return exchange("POST", path, remote, forwarded);
  }

  private MockHttpServletResponse get(String path, String remote)
    throws ServletException, IOException {
    return exchange("GET", path, remote, null);
  }

  private MockHttpServletResponse exchange(
    String method,
    String path,
    String remote,
    String forwarded
  ) throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest(method, path);
    request.setRemoteAddr(remote);
    if (forwarded != null) request.addHeader("X-Forwarded-For", forwarded);

    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, chain);
    return response;
  }

  private static void authenticate(String email) {
    SecurityContextHolder.getContext().setAuthentication(
      new UsernamePasswordAuthenticationToken(
        email,
        null,
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
      )
    );
  }
}
