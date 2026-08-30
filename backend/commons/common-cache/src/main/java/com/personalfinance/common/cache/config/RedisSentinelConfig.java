package com.personalfinance.common.cache.config;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

/**
 * Redis Sentinel configuration for HA.
 * Reads from replicas when possible, writes to master.
 */
@Configuration
public class RedisSentinelConfig {

  @Value("${spring.data.redis.sentinel.master}")
  private String master;

  @Value("${spring.data.redis.sentinel.nodes}")
  private String sentinelNodes;

  @Value("${spring.data.redis.password:}")
  private String password;

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
      .master(master);

    for (String node : sentinelNodes.split(",")) {
      String[] parts = node.trim().split(":");
      sentinelConfig.sentinel(parts[0], Integer.parseInt(parts[1]));
    }

    if (!password.isEmpty()) {
      sentinelConfig.setPassword(RedisPassword.of(password));
    }

    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
      .readFrom(ReadFrom.REPLICA_PREFERRED)
      .commandTimeout(Duration.ofSeconds(3))
      .build();

    return new LettuceConnectionFactory(sentinelConfig, clientConfig);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
    GenericJacksonJsonRedisSerializer jsonSerializer =
      new GenericJacksonJsonRedisSerializer(JsonMapper.builder().build());

    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(jsonSerializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(jsonSerializer);
    return template;
  }
}
