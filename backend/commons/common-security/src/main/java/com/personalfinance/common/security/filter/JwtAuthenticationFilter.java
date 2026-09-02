package com.personalfinance.common.security.filter;

import com.personalfinance.common.cache.enums.CacheKey;
import com.personalfinance.common.cache.service.CacheService;
import com.personalfinance.common.security.context.UserContext;
import com.personalfinance.common.security.jwt.JwtTokenValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * Dual-mode JWT authentication filter.
 *
 * <p>Mode 1 — Gateway-authenticated (X-User-Id header present):
 * The API Gateway has already validated the JWT and checked the Redis session.
 * This filter trusts the X-User-Id header and sets SecurityContext + UserContext directly.
 * No JWT parsing or Redis calls needed.
 *
 * <p>Mode 2 — Direct call (no X-User-Id header, has Authorization header):
 * For local development / testing when calling services directly without Gateway.
 * Validates JWT signature, checks Redis session, then sets SecurityContext + UserContext.
 *
 * <p>Priority: X-User-Id header → JWT token → unauthenticated
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String HEADER_USER_ID = "X-User-Id";

  private final JwtTokenValidator tokenValidator;
  private final CacheService cacheService;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
    throws ServletException, IOException {
    try {
      String gatewayUserId = request.getHeader(HEADER_USER_ID);

      if (StringUtils.hasText(gatewayUserId)) {
        // Mode 1: Request from Gateway — already authenticated, trust header
        authenticateFromGateway(gatewayUserId);
      } else {
        // Mode 2: Direct call — validate JWT
        authenticateFromJwt(request);
      }
    } catch (Exception e) {
      log.debug("JWT authentication failed: {}", e.getMessage());
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      UserContext.clear();
    }
  }

  /**
   * Gateway-authenticated mode: trust X-User-Id header, set SecurityContext + UserContext.
   */
  private void authenticateFromGateway(String userIdStr) {
    try {
      UUID userId = UUID.fromString(userIdStr);
      setAuthentication(userId);
      log.debug("Authenticated from Gateway header: userId={}", userId);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid X-User-Id header value: {}", userIdStr);
    }
  }

  /**
   * Direct call mode: extract Bearer token, validate JWT, check Redis session.
   */
  private void authenticateFromJwt(HttpServletRequest request) {
    String token = extractToken(request);
    if (Objects.isNull(token) || !tokenValidator.isTokenValid(token)) {
      return;
    }

    UUID userId = tokenValidator.extractUserId(token);
    String sessionId = tokenValidator.extractSessionId(token);

    // Validate session — if sessionId is present, check it exists in Redis
    if (Objects.nonNull(sessionId)
      && !cacheService.exists(CacheKey.SESSION.buildKey(sessionId))) {
      log.debug("Session {} has been invalidated (logged out)", sessionId);
      return;
    }

    setAuthentication(userId);
  }

  private void setAuthentication(UUID userId) {
    UsernamePasswordAuthenticationToken authentication =
      new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    UserContext.set(userId);
  }

  private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
