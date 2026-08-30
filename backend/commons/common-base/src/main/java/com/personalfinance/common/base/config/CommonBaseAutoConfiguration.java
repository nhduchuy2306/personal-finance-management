package com.personalfinance.common.base.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for common-base module.
 * Registers HandlerRegistry, GlobalExceptionHandler, and other base components.
 */
@AutoConfiguration
@ComponentScan("com.personalfinance.common.base")
public class CommonBaseAutoConfiguration {
}
