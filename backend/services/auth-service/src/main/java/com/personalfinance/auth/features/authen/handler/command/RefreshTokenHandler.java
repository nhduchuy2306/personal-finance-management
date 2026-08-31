package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.request.RefreshTokenRequest;
import com.personalfinance.auth.model.RefreshToken;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.cache.service.CacheService;
import com.personalfinance.common.security.jwt.JwtProperties;
import com.personalfinance.common.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Refresh token handler — validates refresh token, resolves user,
 * deletes old token (rotation) via beforeTokenGeneration hook,
 * reuses existing sessionId, then delegates new token generation to AbstractAuthHandler.
 */
@Component
public class RefreshTokenHandler extends AbstractAuthHandler<RefreshTokenRequest> {

  private final UserRepository userRepository;

  /**
   * Holds the stored token between resolveUser() and beforeTokenGeneration()
   */
  private final ThreadLocal<RefreshToken> storedTokenHolder = new ThreadLocal<>();

  public RefreshTokenHandler(RefreshTokenRepository refreshTokenRepository,
                             JwtTokenProvider jwtTokenProvider,
                             JwtProperties jwtProperties,
                             CacheService cacheService,
                             UserRepository userRepository) {
    super(refreshTokenRepository, jwtTokenProvider, jwtProperties, cacheService);
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  protected User resolveUser(RefreshTokenRequest request) {
    // Find and validate refresh token
    RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
      .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

    // Check if expired
    if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      refreshTokenRepository.delete(storedToken);
      throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    // Store for deletion in beforeTokenGeneration and sessionId reuse
    storedTokenHolder.set(storedToken);

    // Find user
    return userRepository.findById(storedToken.getUserId())
      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  @Override
  protected String resolveSessionId(RefreshTokenRequest request) {
    // Reuse the same sessionId from the old refresh token
    RefreshToken storedToken = storedTokenHolder.get();
    return Objects.nonNull(storedToken) ? storedToken.getSessionId() : super.resolveSessionId(request);
  }

  @Override
  protected void beforeTokenGeneration(RefreshTokenRequest request, User user) {
    // Delete old refresh token (rotation)
    RefreshToken storedToken = storedTokenHolder.get();
    storedTokenHolder.remove();
    if (storedToken != null) {
      refreshTokenRepository.delete(storedToken);
    }
  }
}
