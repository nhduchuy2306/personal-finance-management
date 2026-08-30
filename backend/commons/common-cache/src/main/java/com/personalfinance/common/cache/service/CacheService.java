package com.personalfinance.common.cache.service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Cache service interface.
 * All Redis operations go through this — never use RedisTemplate directly.
 */
public interface CacheService {

  // ── Basic operations ──
  <T> Optional<T> get(String key, Class<T> type);

  <T> void set(String key, T value, Duration ttl);

  void delete(String key);

  boolean exists(String key);

  // ── Numeric operations (for spending counters) ──
  Long increment(String key, long delta);

  Long decrement(String key, long delta);

  Long getCounter(String key);

  // ── Hash operations (for complex objects) ──
  <T> void hSet(String key, String field, T value);

  <T> Optional<T> hGet(String key, String field, Class<T> type);

  Map<String, Object> hGetAll(String key);

  void hDelete(String key, String... fields);

  // ── Set operations (for dedup/tracking) ──
  void sAdd(String key, String... values);

  boolean sIsMember(String key, String value);

  Set<String> sMembers(String key);

  // ── List operations ──
  <T> void lPush(String key, T value);

  <T> List<T> lRange(String key, long start, long end, Class<T> type);

  // ── TTL management ──
  void expire(String key, Duration ttl);

  Duration getTtl(String key);

  // ── Batch operations ──
  <T> Map<String, T> multiGet(Collection<String> keys, Class<T> type);

  void multiDelete(Collection<String> keys);

  // ── Locking (for distributed coordination) ──
  boolean tryLock(String key, Duration ttl);

  void unlock(String key);

  // ── Pattern operations ──
  Set<String> keys(String pattern);

  void deleteByPattern(String pattern);
}
