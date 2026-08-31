package com.personalfinance.common.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT token provider — generates access and refresh tokens.
 * Access tokens contain a session ID (sid) claim for session validation.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final JwtProperties jwtProperties;

  /**
   * Generate access token WITH sessionId embedded as "sid" claim.
   * The sessionId is used by JwtAuthenticationFilter to validate active sessions.
   */
  public String generateAccessToken(UUID userId, String email, String sessionId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiry());

    return Jwts.builder()
      .subject(userId.toString())
      .claim("email", email)
      .claim("sid", sessionId)
      .issuer(jwtProperties.getIssuer())
      .issuedAt(now)
      .expiration(expiry)
      .signWith(getSigningKey())
      .compact();
  }

  /**
   * Generate refresh token WITHOUT sessionId.
   * Refresh tokens are validated via DB lookup, not session cache.
   */
  public String generateRefreshToken(UUID userId, String email) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiry());

    return Jwts.builder()
      .subject(userId.toString())
      .claim("email", email)
      .issuer(jwtProperties.getIssuer())
      .issuedAt(now)
      .expiration(expiry)
      .signWith(getSigningKey())
      .compact();
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }
}
