package com.personalfinance.common.cache.config;

import com.personalfinance.common.cache.repository.CacheAwareRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
  repositoryBaseClass = CacheAwareRepositoryImpl.class,
  basePackages = "com.personalfinance"
)
public class CacheAwareRepositoryConfig {
}
