package com.personalfinance.auth.features.systemconfig;

import com.personalfinance.auth.features.systemconfig.model.SystemConfig;
import com.personalfinance.common.cache.enums.CacheKey;
import com.personalfinance.common.cache.registry.CacheEvictionRule;
import com.personalfinance.common.cache.registry.ResolvedCacheKey;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cache eviction rule for SystemConfig entity.
 * When a SystemConfig is saved or deleted, automatically evicts the
 * corresponding "system:config:{configName}" cache key.
 */
@Component
public class SystemConfigCacheEvictionRule implements CacheEvictionRule<SystemConfig> {

  @Override
  public Class<SystemConfig> getEntityType() {
    return SystemConfig.class;
  }

  @Override
  public List<ResolvedCacheKey> resolveKeysToEvict(SystemConfig entity) {
    return List.of(
      ResolvedCacheKey.exact(CacheKey.SYSTEM_CONFIG.buildKey(entity.getConfigName().name()))
    );
  }
}
