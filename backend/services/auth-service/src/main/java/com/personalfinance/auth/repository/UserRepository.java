package com.personalfinance.auth.repository;

import com.personalfinance.auth.model.User;
import com.personalfinance.common.cache.repository.CacheAwareRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User repository — shared across all features (authen, profile, telegram, gRPC).
 * Extends CacheAwareRepository for automatic cache eviction on save/delete.
 */
@Repository
public interface UserRepository extends CacheAwareRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  List<User> findByIdIn(List<UUID> ids);
}
