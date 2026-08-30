package com.personalfinance.common.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties.
 * Configured via application.yml: app.jwt.*
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

  /**
   * Secret key for signing JWT tokens
   */
  private String secret = "default-secret-key-change-in-production-must-be-at-least-256-bits-long";

  /**
   * Access token expiry in milliseconds (default: 30 min)
   */
  private long accessTokenExpiry = 1800000;

  /**
   * Refresh token expiry in milliseconds (default: 7 days)
   */
  private long refreshTokenExpiry = 604800000;

  /**
   * Token issuer name
   */
  private String issuer = "personal-finance";
}
