package com.personalfinance.common.cache.registry;

import java.util.List;

public interface CacheEvictionRule<T> {
  /**
   * Entity class mà rule này apply
   */
  Class<T> getEntityType();

  /**
   * Trả về list cache keys cần evict, resolve từ entity instance
   */
  List<ResolvedCacheKey> resolveKeysToEvict(T entity);
}
