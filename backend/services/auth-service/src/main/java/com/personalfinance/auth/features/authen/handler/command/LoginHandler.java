package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.request.LoginRequest;
import com.personalfinance.auth.features.authen.dto.response.AuthResponse;
import com.personalfinance.auth.features.profile.dto.response.ProfileResponse;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.cache.enums.CacheKey;
import com.personalfinance.common.cache.service.CacheService;
import com.personalfinance.common.security.jwt.JwtProperties;
import com.personalfinance.common.security.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Login handler — verifies email+password, delegates token generation to AbstractAuthHandler,
 * caches user profile in Redis.
 */
@Component
public class LoginHandler extends AbstractAuthHandler<LoginRequest> {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CacheService cacheService;

  public LoginHandler(RefreshTokenRepository refreshTokenRepository,
                      JwtTokenProvider jwtTokenProvider,
                      JwtProperties jwtProperties,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      CacheService cacheService) {
    super(refreshTokenRepository, jwtTokenProvider, jwtProperties);
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.cacheService = cacheService;
  }

  @Override
  @Transactional
  protected User resolveUser(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    return user;
  }

  @Override
  public void postHandle(LoginRequest request, AuthResponse response) {
    // Cache user profile (TTL 30min) — this is cache WARMING, not eviction
    cacheService.set(
      CacheKey.USER_PROFILE.buildKey(response.getUserId()),
      ProfileResponse.builder()
        .id(response.getUserId())
        .email(response.getEmail())
        .displayName(response.getDisplayName())
        .build(),
      Duration.ofMinutes(30)
    );
  }
}
