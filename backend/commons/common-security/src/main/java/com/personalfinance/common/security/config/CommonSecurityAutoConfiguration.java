package com.personalfinance.common.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for common-security module.
 * Registers BaseSecurityConfig, JwtAuthenticationFilter, JwtTokenProvider,
 * JwtTokenValidator, JwtProperties.
 */
@AutoConfiguration
@ComponentScan("com.personalfinance.common.security")
public class CommonSecurityAutoConfiguration {
}
