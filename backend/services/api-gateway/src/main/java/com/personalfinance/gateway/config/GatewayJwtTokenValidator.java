package com.personalfinance.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * JWT token validator for Gateway (WebFlux-compatible).
 * Same validation logic as common-security's JwtTokenValidator,
 * but without Servlet API dependencies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayJwtTokenValidator {

  private final GatewayJwtProperties jwtProperties;

  /**
   * Validate JWT token — checks signature and expiry.
   *
   * @return parsed Claims if valid, null if invalid
   */
  public Claims validateAndExtract(String token) {
    try {
      return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
    } catch (ExpiredJwtException e) {
      log.debug("Gateway: JWT token expired: {}", e.getMessage());
      return null;
    } catch (JwtException e) {
      log.debug("Gateway: JWT token invalid: {}", e.getMessage());
      return null;
    }
  }

  public UUID extractUserId(Claims claims) {
    return UUID.fromString(claims.getSubject());
  }

  public String extractEmail(Claims claims) {
    return claims.get("email", String.class);
  }

  public String extractSessionId(Claims claims) {
    return claims.get("sid", String.class);
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }
}
