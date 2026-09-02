package com.personalfinance.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties for Gateway.
 * Uses same prefix as auth-service's JwtProperties so YAML config is shared.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class GatewayJwtProperties {

  /**
   * Secret key for validating JWT tokens (must match auth-service's secret)
   */
  private String secret = "default-secret-key-change-in-production-must-be-at-least-256-bits-long";

  /**
   * Token issuer name (must match auth-service's issuer)
   */
  private String issuer = "personal-finance";
}
