package com.crm.karma.configs;

import com.crm.karma.filters.JwtAuthFilter;
import com.crm.karma.filters.RateLimitFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(RateLimitProperties.class)
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    JwtAuthFilter jwtAuthFilter,
    RateLimitFilter rateLimitFilter
  ) {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .cors(Customizer.withDefaults())
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
      .addFilterAfter(rateLimitFilter, JwtAuthFilter.class)
      .authorizeHttpRequests(auth -> auth
        // SSE/async timeout and error dispatches reuse the original URI
        // (/notifications/stream) without a SecurityContext: the JWT filter
        // skips async dispatches and this API is stateless. Denying them
        // leaves the async connection open and starves the browser.
        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR)
        .permitAll()
        // /error must be public: ResponseStatusException (and other MVC errors)
        // are forwarded here; if secured, every error becomes 403 Forbidden.
        .requestMatchers(
          "/error",
          "/auth/**",
          "/swagger-ui/**",
          "/v3/api-docs/**"
        )
        .permitAll()
        .anyRequest()
        .authenticated()
      );

    return http.build();
  }

  /**
   * The filter also lives in the security chain, after the JWT filter.
   * Leaving the servlet registration on would consume two tokens per request.
   */
  @Bean
  public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
    RateLimitFilter rateLimitFilter
  ) {
    FilterRegistrationBean<RateLimitFilter> registration =
      new FilterRegistrationBean<>(rateLimitFilter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOriginPattern("http://localhost:3000");
    configuration.addAllowedHeader("*");
    configuration.addAllowedMethod("*");
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
