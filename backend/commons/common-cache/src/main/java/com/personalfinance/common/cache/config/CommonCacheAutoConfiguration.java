package com.personalfinance.common.cache.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for common-cache module.
 * Registers RedisSentinelConfig, RedisCacheService, CacheEvictionEngine, CacheEvictionListener.
 */
@AutoConfiguration
@ComponentScan("com.personalfinance.common.cache")
public class CommonCacheAutoConfiguration {
}
