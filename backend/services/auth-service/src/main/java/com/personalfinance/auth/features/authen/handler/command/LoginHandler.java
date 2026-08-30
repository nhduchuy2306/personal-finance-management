package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.request.LoginRequest;
import com.personalfinance.auth.features.authen.dto.response.AuthResponse;
import com.personalfinance.auth.features.profile.dto.response.ProfileResponse;
import com.personalfinance.auth.model.RefreshToken;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.base.handler.AbstractHandler;
import com.personalfinance.common.cache.key.CacheKeyBuilder;
import com.personalfinance.common.cache.service.CacheService;
import com.personalfinance.common.security.jwt.JwtProperties;
import com.personalfinance.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Login handler — verifies email+password, generates access+refresh tokens,
 * caches user profile in Redis.
 */
@Component
@RequiredArgsConstructor
public class LoginHandler extends AbstractHandler<LoginRequest, AuthResponse> {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtProperties jwtProperties;
  private final CacheService cacheService;

  @Override
  @Transactional
  public AuthResponse doHandle(LoginRequest request) {
    // Find user by email
    User user = userRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

    // Verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    // Generate JWT pair
    String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

    // Save refresh token
    RefreshToken tokenEntity = RefreshToken.builder()
      .userId(user.getId())
      .token(refreshToken)
      .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiry() / 1000))
      .build();
    refreshTokenRepository.save(tokenEntity);

    return AuthResponse.builder()
      .accessToken(accessToken)
      .refreshToken(refreshToken)
      .userId(user.getId())
      .email(user.getEmail())
      .displayName(user.getDisplayName())
      .build();
  }

  @Override
  public void postHandle(LoginRequest request, AuthResponse response) {
    // Cache user profile (TTL 30min)
    cacheService.set(
      CacheKeyBuilder.userProfile(response.getUserId()),
      ProfileResponse.builder()
        .id(response.getUserId())
        .email(response.getEmail())
        .displayName(response.getDisplayName())
        .build(),
      Duration.ofMinutes(30)
    );
  }
}
