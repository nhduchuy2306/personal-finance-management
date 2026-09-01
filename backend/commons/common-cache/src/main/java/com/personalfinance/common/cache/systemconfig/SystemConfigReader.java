package com.personalfinance.common.cache.systemconfig;

import com.personalfinance.common.cache.enums.CacheKey;
import com.personalfinance.common.cache.enums.ConfigName;
import com.personalfinance.common.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Read-only accessor for system configuration values.
 * Reads from Redis cache first, falls back to enum default if not found.
 *
 * <p><b>Important:</b> This class does NOT have DB access. Config values are
 * populated into Redis by auth-service's SystemConfigCacheWarmer (startup + daily cron).
 *
 * <p>Usage:
 * <pre>
 *   Duration ttl = systemConfigReader.getAsDuration(ConfigName.CACHE_TTL_USER_PROFILE);
 *   int threshold = systemConfigReader.getAsInt(ConfigName.ALERT_THRESHOLD_PERCENT);
 *   boolean enabled = systemConfigReader.getAsBoolean(ConfigName.BUDGET_AUTO_FILL_ENABLED);
 * </pre>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigReader {

  private final CacheService cacheService;

  /**
   * Get config value as String.
   * Flow: Redis cache → enum default value.
   */
  public String getValue(ConfigName name) {
    String cacheKey = CacheKey.SYSTEM_CONFIG.buildKey(name.name());

    return cacheService.get(cacheKey, String.class)
      .orElseGet(() -> {
        log.debug("System config cache miss for [{}], using default: {}", name, name.getDefaultValue());
        return name.getDefaultValue();
      });
  }

  /**
   * Get config value parsed as int.
   * Returns enum default if parse fails.
   */
  public int getAsInt(ConfigName name) {
    try {
      return Integer.parseInt(getValue(name));
    } catch (NumberFormatException e) {
      log.warn("Config {} is not a valid int, using default: {}", name, name.getDefaultValue());
      return Integer.parseInt(name.getDefaultValue());
    }
  }

  /**
   * Get config value parsed as long.
   * Returns enum default if parse fails.
   */
  public long getAsLong(ConfigName name) {
    try {
      return Long.parseLong(getValue(name));
    } catch (NumberFormatException e) {
      log.warn("Config {} is not a valid long, using default: {}", name, name.getDefaultValue());
      return Long.parseLong(name.getDefaultValue());
    }
  }

  /**
   * Get config value parsed as boolean.
   */
  public boolean getAsBoolean(ConfigName name) {
    return Boolean.parseBoolean(getValue(name));
  }

  /**
   * Get config value parsed as Duration (value interpreted as seconds).
   * Useful for cache TTL configs.
   */
  public Duration getAsDuration(ConfigName name) {
    return Duration.ofSeconds(getAsLong(name));
  }
}
