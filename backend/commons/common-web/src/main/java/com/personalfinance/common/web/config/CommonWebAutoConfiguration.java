package com.personalfinance.common.web.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for common-web module.
 * Registers CorsConfig, JacksonConfig, SwaggerConfig, RequestLoggingFilter.
 */
@AutoConfiguration
@ComponentScan("com.personalfinance.common.web")
public class CommonWebAutoConfiguration {
}
