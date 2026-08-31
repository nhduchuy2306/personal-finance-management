package com.personalfinance.common.cache.registry;

import com.personalfinance.common.cache.enums.EvictStrategy;
import com.personalfinance.common.cache.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CacheEvictionRegistry {
  private final Map<Class<?>, CacheEvictionRule<?>> ruleMap;
  private final CacheService cacheService;

  public CacheEvictionRegistry(List<CacheEvictionRule<?>> rules, CacheService cacheService) {
    this.cacheService = cacheService;
    this.ruleMap = rules.stream()
      .collect(Collectors.toMap(CacheEvictionRule::getEntityType, Function.identity()));
  }

  @SuppressWarnings("unchecked")
  public <T> void evictFor(T entity) {
    var clazz = Hibernate.getClass(entity);
    CacheEvictionRule<T> rule = (CacheEvictionRule<T>) ruleMap.get(clazz);
    if (Objects.isNull(rule)) {
      log.trace("No eviction rule for {}", clazz.getSimpleName());
      return;
    }

    List<ResolvedCacheKey> keys = rule.resolveKeysToEvict(entity);
    for (ResolvedCacheKey key : keys) {
      try {
        if (key.strategy() == EvictStrategy.EXACT) {
          cacheService.delete(key.key());
        } else {
          cacheService.deleteByPattern(key.key());
        }
        log.debug("Cache evicted [{}]: {}", key.strategy(), key.key());
      } catch (Exception e) {
        log.warn("Cache eviction failed for {}: {}", key.key(), e.getMessage());
      }
    }
  }
}
