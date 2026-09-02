package com.personalfinance.gateway.config;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import java.time.Duration;

/**
 * Reactive Redis Sentinel configuration for Gateway.
 * Uses ReactiveStringRedisTemplate (non-blocking) instead of RedisTemplate (blocking)
 * to work correctly in WebFlux/Netty event loop.
 */
@Configuration
public class ReactiveRedisConfig {

  @Value("${spring.data.redis.sentinel.master}")
  private String master;

  @Value("${spring.data.redis.sentinel.nodes}")
  private String sentinelNodes;

  @Value("${spring.data.redis.password:}")
  private String password;

  @Bean
  @Primary
  public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
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
  public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
    ReactiveRedisConnectionFactory factory) {
    return new ReactiveStringRedisTemplate(factory);
  }
}
