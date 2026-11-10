package com.crm.karma.filters;

import com.crm.karma.configs.RateLimitProperties;
import com.crm.karma.services.ClientIpResolver;
import com.crm.karma.services.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.OptionalLong;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

  public static final String RATE_LIMIT_CODE = "RATE_LIMIT";
  public static final String RATE_LIMIT_DETAIL = "Too many requests. Try again later.";

  private final RateLimitProperties properties;
  private final ClientIpResolver clientIpResolver;
  private final RateLimitService rateLimitService;

  public RateLimitFilter(RateLimitProperties properties, ClientIpResolver clientIpResolver, RateLimitService rateLimitService) {
    this.properties = properties;
    this.clientIpResolver = clientIpResolver;
    this.rateLimitService = rateLimitService;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = path(request);
    return "/error".equals(path) || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
    OptionalLong retryAfter = limit(request);

    if (retryAfter.isPresent()) {
      reject(response, retryAfter.getAsLong());
      return;
    }

    filterChain.doFilter(request, response);
  }

  private OptionalLong limit(HttpServletRequest request) {
    String path = path(request);
    String method = request.getMethod();
    String ip = clientIpResolver.resolve(request);

    if ("POST".equalsIgnoreCase(method) && "/auth/login".equals(path)) {
      return rateLimitService.tryConsume("login:" + ip, properties.getLoginPerMinute(), Duration.ofMinutes(1));
    }

    if ("POST".equalsIgnoreCase(method) && "/auth/register".equals(path)) {
      return rateLimitService.tryConsume("register:" + ip, properties.getRegisterPerHour(), Duration.ofHours(1));
    }

    String user = authenticatedUser();
    if (user != null) {
      return rateLimitService.tryConsume("user:" + user, properties.getAuthenticatedPerMinute(), Duration.ofMinutes(1));
    }

    return rateLimitService.tryConsume("ip:" + ip, properties.getAnonymousPerMinute(), Duration.ofMinutes(1));
  }

  /**
   * JWT subject is the user e-mail. The token has no numeric id, and looking
   * the account up on every request would hit the database under abuse.
   */
  private static String authenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) return null;

    boolean anonymous = authentication.getAuthorities().stream().anyMatch(authority -> "ROLE_ANONYMOUS".equals(authority.getAuthority()));

    if (anonymous) return null;

    String name = authentication.getName();
    if (name == null || name.isBlank() || "anonymousUser".equals(name)) return null;

    return name;
  }

  private static void reject(HttpServletResponse response, long retryAfterSeconds) throws IOException {
    response.setStatus(429);
    response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write("{\"status\":429,\"code\":\"" + RATE_LIMIT_CODE + "\",\"detail\":\"" + RATE_LIMIT_DETAIL + "\"}");
  }

  private static String path(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String context = request.getContextPath();

    if (context != null && !context.isEmpty() && uri.startsWith(context)) {
      return uri.substring(context.length());
    }

    return uri;
  }
}
