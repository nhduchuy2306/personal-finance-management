package com.personalfinance.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Gateway security properties — configurable public paths.
 * Paths in this list bypass JWT authentication.
 *
 * <p>Supports Ant-style patterns (e.g., /api/v1/auth/**).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.gateway")
public class GatewaySecurityProperties {

  /**
   * Public paths that bypass authentication.
   * Supports Ant-style wildcards: ?, *, **
   */
  private List<String> publicPaths = new ArrayList<>();
}
