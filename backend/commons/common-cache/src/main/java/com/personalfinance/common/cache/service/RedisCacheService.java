package com.personalfinance.common.cache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis implementation of CacheService.
 * All methods catch exceptions and degrade gracefully.
 * Cache failure NEVER breaks the app — log warning and return empty/null.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService implements CacheService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public <T> Optional<T> get(String key, Class<T> type) {
    try {
      Object value = redisTemplate.opsForValue().get(key);
      if (value == null) return Optional.empty();
      return Optional.of(objectMapper.convertValue(value, type));
    } catch (Exception e) {
      log.warn("Redis GET failed for key {}: {}", key, e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public <T> void set(String key, T value, Duration ttl) {
    try {
      redisTemplate.opsForValue().set(key, value, ttl);
    } catch (Exception e) {
      log.warn("Redis SET failed for key {}: {}", key, e.getMessage());
    }
  }

  @Override
  public void delete(String key) {
    try {
      redisTemplate.delete(key);
    } catch (Exception e) {
      log.warn("Redis DELETE failed for key {}: {}", key, e.getMessage());
    }
  }

  @Override
  public boolean exists(String key) {
    try {
      return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    } catch (Exception e) {
      log.warn("Redis EXISTS failed for key {}: {}", key, e.getMessage());
      return false;
    }
  }

  @Override
  public Long increment(String key, long delta) {
    try {
      return redisTemplate.opsForValue().increment(key, delta);
    } catch (Exception e) {
      log.warn("Redis INCREMENT failed for key {}: {}", key, e.getMessage());
      return null;
    }
  }

  @Override
  public Long decrement(String key, long delta) {
    try {
      return redisTemplate.opsForValue().decrement(key, delta);
    } catch (Exception e) {
      log.warn("Redis DECREMENT failed for key {}: {}", key, e.getMessage());
      return null;
    }
  }

  @Override
  public Long getCounter(String key) {
    try {
      Object value = redisTemplate.opsForValue().get(key);
      if (value == null) return null;
      return Long.parseLong(value.toString());
    } catch (Exception e) {
      log.warn("Redis GET_COUNTER failed for key {}: {}", key, e.getMessage());
      return null;
    }
  }

  @Override
  public <T> void hSet(String key, String field, T value) {
    try {
      redisTemplate.opsForHash().put(key, field, value);
    } catch (Exception e) {
      log.warn("Redis HSET failed for key {}:{}: {}", key, field, e.getMessage());
    }
  }

  @Override
  public <T> Optional<T> hGet(String key, String field, Class<T> type) {
    try {
      Object value = redisTemplate.opsForHash().get(key, field);
      if (value == null) return Optional.empty();
      return Optional.of(objectMapper.convertValue(value, type));
    } catch (Exception e) {
      log.warn("Redis HGET failed for key {}:{}: {}", key, field, e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public Map<String, Object> hGetAll(String key) {
    try {
      Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
      return entries.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
    } catch (Exception e) {
      log.warn("Redis HGETALL failed for key {}: {}", key, e.getMessage());
      return Collections.emptyMap();
    }
  }

  @Override
  public void hDelete(String key, String... fields) {
    try {
      redisTemplate.opsForHash().delete(key, (Object[]) fields);
    } catch (Exception e) {
      log.warn("Redis HDEL failed for key {}: {}", key, e.getMessage());
    }
  }

  @Override
  public void sAdd(String key, String... values) {
    try {
      redisTemplate.opsForSet().add(key, (Object[])values);
    } catch (Exception e) {
      log.warn("Redis SADD failed for key {}: {}", key, e.getMessage());
    }
  }

  @Override
  public boolean sIsMember(String key, String value) {
    try {
      return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    } catch (Exception e) {
      log.warn("Redis SISMEMBER failed for key {}: {}", key, e.getMessage());
      return false;
    }
  }

  @Override
  public Set<String> sMembers(String key) {
    try {
      Set<Object> members = redisTemplate.opsForSet().members(key);
      if (members == null) return Collections.emptySet();
      return members.stream().map(Object::toString).collect(Collectors.toSet());
    } catch (Exception e) {
      log.warn("Redis SMEMBERS failed for key {}: {}", key, e.getMessage());
      return Collections.emptySet();
    }
  }

  @Override
  public <T> void lPush(String key, T value) {
    try {
      redisTemplate.opsForList().leftPush(key, value);
    } catch (Exception e) {
      log.warn("Redis LPUSH failed for key {}: {}", key, e.getMessage());
    }
  }

  @Override
  public <T> List<T> lRange(String key, long start, long end, Class<T> type) {
    try {
      List<Object> values = redisTemplate.opsForList().range(key, start, end);
      if (values == null) return Collections.emptyList();
      return values.stream()
        .map(v -> objectMapper.convertValue(v, type))
        .collect(Collectors.toList());
    } catch (Exception e) {
      log.warn("Redis LRANGE failed for key {}: {}", key, e.getMessage());
      return Collections.emptyList();
    }
  }

  @Override
  public void expire(String key, Duration ttl) {
    try {
      redisTemplate.expire(key, Expiration.seconds(ttl.getSeconds()));
    } catch (Exception e) {
      log.warn("Redis EXPIRE failed for key {}: {}", key, e.getMessage());
    }
  }

  @Override
  public Duration getTtl(String key) {
    try {
      Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
      return ttl != null && ttl > 0 ? Duration.ofSeconds(ttl) : Duration.ZERO;
    } catch (Exception e) {
      log.warn("Redis TTL failed for key {}: {}", key, e.getMessage());
      return Duration.ZERO;
    }
  }

  @Override
  public <T> Map<String, T> multiGet(Collection<String> keys, Class<T> type) {
    try {
      List<String> keyList = new ArrayList<>(keys);
      List<Object> values = redisTemplate.opsForValue().multiGet(keyList);
      if (values == null) return Collections.emptyMap();
      Map<String, T> result = new HashMap<>();
      for (int i = 0; i < keyList.size(); i++) {
        Object val = values.get(i);
        if (val != null) {
          result.put(keyList.get(i), objectMapper.convertValue(val, type));
        }
      }
      return result;
    } catch (Exception e) {
      log.warn("Redis MGET failed: {}", e.getMessage());
      return Collections.emptyMap();
    }
  }

  @Override
  public void multiDelete(Collection<String> keys) {
    try {
      redisTemplate.delete(keys);
    } catch (Exception e) {
      log.warn("Redis MDEL failed: {}", e.getMessage());
    }
  }

  @Override
  public boolean tryLock(String key, Duration ttl) {
    try {
      Boolean result = redisTemplate.opsForValue().setIfAbsent("lock:" + key, "1", ttl);
      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      log.warn("Redis LOCK failed for key {}: {}", key, e.getMessage());
      return false;
    }
  }

  @Override
  public void unlock(String key) {
    try {
      redisTemplate.delete("lock:" + key);
    } catch (Exception e) {
      log.warn("Redis UNLOCK failed for key {}: {}", key, e.getMessage());
    }
  }

  @Override
  public Set<String> keys(String pattern) {
    try {
      Set<String> result = redisTemplate.keys(pattern);
      return result != null ? result : Collections.emptySet();
    } catch (Exception e) {
      log.warn("Redis KEYS failed for pattern {}: {}", pattern, e.getMessage());
      return Collections.emptySet();
    }
  }

  @Override
  public void deleteByPattern(String pattern) {
    try {
      Set<String> matchingKeys = keys(pattern);
      if (!matchingKeys.isEmpty()) {
        redisTemplate.delete(matchingKeys);
      }
    } catch (Exception e) {
      log.warn("Redis DELETE_BY_PATTERN failed for {}: {}", pattern, e.getMessage());
    }
  }
}
