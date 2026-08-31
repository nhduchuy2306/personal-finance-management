package com.personalfinance.common.cache.registry;

import com.personalfinance.common.cache.enums.EvictStrategy;
import lombok.Builder;

@Builder(toBuilder = true)
public record ResolvedCacheKey(String key, EvictStrategy strategy) {
  public static ResolvedCacheKey exact(String key) {
    return new ResolvedCacheKey(key, EvictStrategy.EXACT);
  }

  public static ResolvedCacheKey pattern(String key) {
    return new ResolvedCacheKey(key, EvictStrategy.PATTERN);
  }
}
