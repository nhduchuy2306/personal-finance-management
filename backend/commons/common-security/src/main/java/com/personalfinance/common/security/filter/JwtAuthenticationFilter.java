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
import java.util.UUID;

/**
 * JWT authentication filter.
 * Extracts Bearer token, validates JWT signature, checks session in Redis,
 * then sets SecurityContext and UserContext.
 *
 * <p>Session validation: After JWT is valid, checks if the session (sid claim)
 * still exists in Redis. If not (user logged out), authentication is rejected.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenValidator tokenValidator;
  private final CacheService cacheService;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
    throws ServletException, IOException {
    try {
      String token = extractToken(request);
      if (token != null && tokenValidator.isTokenValid(token)) {
        UUID userId = tokenValidator.extractUserId(token);
        String sessionId = tokenValidator.extractSessionId(token);

        // Validate session — if sessionId is present, check it exists in Redis
        if (sessionId != null
          && !cacheService.exists(CacheKey.SESSION.buildKey(sessionId))) {
          log.debug("Session {} has been invalidated (logged out)", sessionId);
          // Skip authentication — user is logged out
        } else {
          String email = tokenValidator.extractEmail(token);

          UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
          SecurityContextHolder.getContext().setAuthentication(authentication);

          UserContext.set(userId);
        }
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

  private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
