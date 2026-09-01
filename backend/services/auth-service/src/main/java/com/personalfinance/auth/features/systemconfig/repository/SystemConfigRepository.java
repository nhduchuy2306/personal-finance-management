package com.personalfinance.auth.features.systemconfig.repository;

import com.personalfinance.auth.features.systemconfig.model.SystemConfig;
import com.personalfinance.common.cache.enums.ConfigName;
import com.personalfinance.common.cache.repository.CacheAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for SystemConfig entity.
 * Extends CacheAwareRepository → automatically publishes EntityChangedEvent
 * on save/delete, triggering cache eviction via SystemConfigCacheEvictionRule.
 */
@Repository
public interface SystemConfigRepository extends CacheAwareRepository<SystemConfig, UUID> {

  Optional<SystemConfig> findByConfigName(ConfigName configName);
}
