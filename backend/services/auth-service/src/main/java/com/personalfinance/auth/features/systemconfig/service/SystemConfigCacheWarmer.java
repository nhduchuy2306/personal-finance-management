package com.personalfinance.auth.features.systemconfig.service;

import com.personalfinance.auth.features.systemconfig.model.SystemConfig;
import com.personalfinance.auth.features.systemconfig.repository.SystemConfigRepository;
import com.personalfinance.common.cache.enums.CacheKey;
import com.personalfinance.common.cache.enums.ConfigName;
import com.personalfinance.common.cache.service.CacheService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * Cache warmer for system config values.
 * Loads ALL configs from DB → sets Redis with 1-day TTL.
 *
 * <p>Triggers:
 * <ul>
 *   <li>{@code @PostConstruct} — warms cache on auth-service startup</li>
 *   <li>Quartz cron job ({@code SystemConfigWarmUpJob}) — daily at 3:00 AM</li>
 *   <li>{@code warmAll()} — called manually via admin API (WarmUpSystemConfigHandler)</li>
 * </ul>
 *
 * <p>Other services read config via SystemConfigReader (in common-cache),
 * which only reads Redis → enum default. This warmer ensures Redis is populated.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigCacheWarmer {

  private static final Duration CONFIG_CACHE_TTL = Duration.ofDays(1);

  private final SystemConfigRepository systemConfigRepository;
  private final CacheService cacheService;

  /**
   * Warm ALL system config values into Redis cache.
   * For each ConfigName enum:
   * - If DB has a value → cache it
   * - If DB has no value → cache the enum default
   *
   * @return number of config entries warmed
   */
  public int warmAll() {
    log.info("System config cache warming started...");

    List<SystemConfig> dbConfigs = systemConfigRepository.findAll();

    int count = 0;
    for (ConfigName configName : ConfigName.values()) {
      String value = dbConfigs.stream()
        .filter(c -> c.getConfigName() == configName)
        .map(SystemConfig::getValue)
        .findFirst()
        .orElse(configName.getDefaultValue());

      String cacheKey = CacheKey.SYSTEM_CONFIG.buildKey(configName.name());
      cacheService.set(cacheKey, value, CONFIG_CACHE_TTL);
      count++;
    }

    log.info("System config cache warming completed — {} entries warmed (TTL={})", count, CONFIG_CACHE_TTL);
    return count;
  }

  /**
   * Warm cache on auth-service startup.
   */
  @PostConstruct
  public void onStartup() {
    try {
      warmAll();
    } catch (Exception e) {
      // Don't fail startup if Redis is unavailable
      log.warn("System config cache warming failed on startup: {}", e.getMessage());
    }
  }
}

