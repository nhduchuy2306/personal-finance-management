package com.personalfinance.common.security.jwt;

import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
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
 * JWT token validator — validates and extracts claims from tokens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

  private final JwtProperties jwtProperties;

  public UUID extractUserId(String token) {
    Claims claims = parseClaims(token);
    return UUID.fromString(claims.getSubject());
  }

  public String extractEmail(String token) {
    Claims claims = parseClaims(token);
    return claims.get("email", String.class);
  }

  public String extractSessionId(String token) {
    Claims claims = parseClaims(token);
    return claims.get("sid", String.class);
  }

  public boolean isTokenValid(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private Claims parseClaims(String token) {
    try {
      return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
    } catch (ExpiredJwtException e) {
      log.warn("JWT token expired: {}", e.getMessage());
      throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
    } catch (JwtException e) {
      log.warn("JWT token invalid: {}", e.getMessage());
      throw new BusinessException(ErrorCode.TOKEN_INVALID);
    }
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }
}
