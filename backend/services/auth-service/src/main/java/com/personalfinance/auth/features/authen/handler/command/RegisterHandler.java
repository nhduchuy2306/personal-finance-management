package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.request.RegisterRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Register handler — validates email uniqueness, hashes password (BCrypt),
 * saves user, generates JWT pair, returns AuthResponse.
 */
@Component
@RequiredArgsConstructor
public class RegisterHandler extends AbstractHandler<RegisterRequest, AuthResponse> {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtProperties jwtProperties;

  @Override
  public void preHandle(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
  }

  @Override
  @Transactional
  public AuthResponse doHandle(RegisterRequest request) {
    // Hash password and save user
    User user = User.builder()
      .email(request.getEmail())
      .passwordHash(passwordEncoder.encode(request.getPassword()))
      .displayName(request.getDisplayName())
      .isActive(true)
      .build();
    user = userRepository.save(user);

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
}
