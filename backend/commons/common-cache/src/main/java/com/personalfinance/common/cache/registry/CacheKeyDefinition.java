package com.personalfinance.common.cache.registry;

import com.personalfinance.common.cache.enums.EvictStrategy;

public interface CacheKeyDefinition {
  /**
   * Key pattern dùng %s cho placeholder. VD: "spending:daily:%s:%s:%s"
   */
  String getPattern();

  /**
   * EXACT = xóa đúng key, PATTERN = xóa wildcard
   */
  EvictStrategy getEvictStrategy();

  /**
   * Build key cụ thể từ args
   */
  default String buildKey(Object... args) {
    return String.format(getPattern(), args);
  }

  /**
   * Build eviction key — thay args không biết bằng *
   */
  default String buildEvictKey(Object... knownArgs) {
    // Đếm số %s trong pattern
    long placeholderCount = getPattern().chars()
      .filter(c -> c == '%').count();

    Object[] fullArgs = new Object[(int) placeholderCount];
    for (int i = 0; i < fullArgs.length; i++) {
      fullArgs[i] = (i < knownArgs.length && knownArgs[i] != null)
        ? knownArgs[i] : "*";
    }
    return String.format(getPattern().replace("%s", "%s"), fullArgs);
  }
}
