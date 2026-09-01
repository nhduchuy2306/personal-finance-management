package com.personalfinance.auth.repository;

import com.personalfinance.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * RefreshToken repository — used by authen feature for token rotation.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByToken(String token);

  void deleteByUserId(UUID userId);

  void deleteByToken(String token);

  /**
   * Bulk delete expired refresh tokens.
   * Used by ExpiredTokenCleanupJob to prevent table growth.
   *
   * @return number of deleted rows
   */
  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
  int deleteAllByExpiresAtBefore(LocalDateTime cutoff);
}
