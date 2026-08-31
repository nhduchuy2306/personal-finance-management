package com.personalfinance.auth.config;

import com.personalfinance.auth.model.User;
import com.personalfinance.common.cache.enums.CacheKey;
import com.personalfinance.common.cache.registry.CacheEvictionRule;
import com.personalfinance.common.cache.registry.ResolvedCacheKey;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cache eviction rule for User entity.
 * When a User is saved or deleted, automatically evicts the user profile cache key.
 */
@Component
public class UserCacheEvictionRule implements CacheEvictionRule<User> {

  @Override
  public Class<User> getEntityType() {
    return User.class;
  }

  @Override
  public List<ResolvedCacheKey> resolveKeysToEvict(User entity) {
    return List.of(
      ResolvedCacheKey.exact(CacheKey.USER_PROFILE.buildKey(entity.getId()))
    );
  }
}
