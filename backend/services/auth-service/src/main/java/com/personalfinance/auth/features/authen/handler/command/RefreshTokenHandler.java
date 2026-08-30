package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.request.RefreshTokenRequest;
import com.personalfinance.auth.features.authen.dto.response.AuthResponse;
import com.personalfinance.auth.model.RefreshToken;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.base.handler.AbstractHandler;
import com.personalfinance.common.security.jwt.JwtProperties;
import com.personalfinance.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Refresh token handler — validates refresh token, rotates (delete old, create new),
 * returns new JWT pair.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenHandler extends AbstractHandler<RefreshTokenRequest, AuthResponse> {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtProperties jwtProperties;

  @Override
  @Transactional
  public AuthResponse doHandle(RefreshTokenRequest request) {
    // Find and validate refresh token
    RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
      .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

    // Check if expired
    if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      refreshTokenRepository.delete(storedToken);
      throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    // Find user
    User user = userRepository.findById(storedToken.getUserId())
      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // Delete old refresh token (rotation)
    refreshTokenRepository.delete(storedToken);

    // Generate new JWT pair
    String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

    // Save new refresh token
    RefreshToken newTokenEntity = RefreshToken.builder()
      .userId(user.getId())
      .token(newRefreshToken)
      .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiry() / 1000))
      .build();
    refreshTokenRepository.save(newTokenEntity);

    return AuthResponse.builder()
      .accessToken(newAccessToken)
      .refreshToken(newRefreshToken)
      .userId(user.getId())
      .email(user.getEmail())
      .displayName(user.getDisplayName())
      .build();
  }
}
