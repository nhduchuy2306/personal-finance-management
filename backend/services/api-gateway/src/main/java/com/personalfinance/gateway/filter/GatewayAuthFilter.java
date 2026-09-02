package com.personalfinance.gateway.filter;

import com.personalfinance.gateway.config.GatewayJwtTokenValidator;
import com.personalfinance.gateway.config.GatewaySecurityProperties;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Gateway authentication filter — validates JWT and forwards user identity to downstream services.
 *
 * <p>Flow:
 * <ol>
 *   <li>Check if request path is public → skip authentication</li>
 *   <li>Extract Bearer token from Authorization header</li>
 *   <li>Validate JWT (signature + expiry)</li>
 *   <li>Check Redis for active session (sid claim)</li>
 *   <li>Strip incoming Authorization, X-User-Id, X-User-Email headers (anti-spoof)</li>
 *   <li>Add X-User-Id and X-User-Email headers for downstream services</li>
 *   <li>Forward mutated request</li>
 * </ol>
 *
 * <p>Security: downstream services trust X-User-Id header because:
 * <ul>
 *   <li>Gateway strips any incoming X-User-Id before adding the validated one</li>
 *   <li>In production, downstream services are only reachable via internal Docker network</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayAuthFilter implements GlobalFilter, Ordered {

  public static final String HEADER_USER_ID = "X-User-Id";
  public static final String HEADER_USER_EMAIL = "X-User-Email";

  private static final String SESSION_KEY_PREFIX = "session:";

  private final GatewayJwtTokenValidator jwtTokenValidator;
  private final GatewaySecurityProperties securityProperties;
  private final ReactiveStringRedisTemplate redisTemplate;

  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  @NullMarked
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getURI().getPath();

    // 1. Skip authentication for public paths
    if (isPublicPath(path)) {
      return chain.filter(stripSpoofHeaders(exchange));
    }

    // 2. Extract Bearer token
    String token = extractToken(request);
    if (token == null) {
      return unauthorized(exchange, "Missing or invalid Authorization header");
    }

    // 3. Validate JWT
    Claims claims = jwtTokenValidator.validateAndExtract(token);
    if (claims == null) {
      return unauthorized(exchange, "Invalid or expired JWT token");
    }

    UUID userId = jwtTokenValidator.extractUserId(claims);
    String email = jwtTokenValidator.extractEmail(claims);
    String sessionId = jwtTokenValidator.extractSessionId(claims);

    // 4. Check Redis session (if sessionId present in token)
    if (sessionId == null) {
      // No session claim — proceed with user info
      return chain.filter(mutateRequest(exchange, userId, email));
    }

    String sessionKey = SESSION_KEY_PREFIX + sessionId;
    return redisTemplate.hasKey(sessionKey)
      .defaultIfEmpty(false)
      .flatMap(exists -> {
        if (exists) {
          // Session active — forward with user info
          return chain.filter(mutateRequest(exchange, userId, email));
        } else {
          // Session invalidated (user logged out)
          log.debug("Gateway: Session {} has been invalidated (logged out)", sessionId);
          return unauthorized(exchange, "Session has been invalidated");
        }
      })
      .onErrorResume(e -> {
        // Redis failure — fail open (allow request, let downstream handle)
        log.warn("Gateway: Redis session check failed, allowing request: {}", e.getMessage());
        return chain.filter(mutateRequest(exchange, userId, email));
      });
  }

  @Override
  public int getOrder() {
    // Run early in the filter chain, before routing
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }

  /**
   * Check if the request path matches any configured public path pattern.
   */
  private boolean isPublicPath(String path) {
    return securityProperties.getPublicPaths().stream()
      .anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  /**
   * Extract Bearer token from Authorization header.
   */
  private String extractToken(ServerHttpRequest request) {
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  /**
   * Mutate the request: strip sensitive headers and add user identity headers.
   */
  private ServerWebExchange mutateRequest(ServerWebExchange exchange, UUID userId, String email) {
    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
      // Strip original Authorization (downstream doesn't need it)
      .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
      // Strip any spoofed identity headers from client
      .headers(headers -> headers.remove(HEADER_USER_ID))
      .headers(headers -> headers.remove(HEADER_USER_EMAIL))
      // Add validated identity headers
      .header(HEADER_USER_ID, userId.toString())
      .header(HEADER_USER_EMAIL, email != null ? email : "")
      .build();

    return exchange.mutate().request(mutatedRequest).build();
  }

  /**
   * Strip spoofed identity headers for public paths too (safety measure).
   */
  private ServerWebExchange stripSpoofHeaders(ServerWebExchange exchange) {
    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
      .headers(headers -> headers.remove(HEADER_USER_ID))
      .headers(headers -> headers.remove(HEADER_USER_EMAIL))
      .build();

    return exchange.mutate().request(mutatedRequest).build();
  }

  /**
   * Return 401 Unauthorized response.
   */
  private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
    log.debug("Gateway: Unauthorized — {}", reason);
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return response.setComplete();
  }
}
