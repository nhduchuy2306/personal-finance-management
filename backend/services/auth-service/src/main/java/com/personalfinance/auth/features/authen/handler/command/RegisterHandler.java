package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.request.RegisterRequest;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.cache.service.CacheService;
import com.personalfinance.common.security.jwt.JwtProperties;
import com.personalfinance.common.security.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Register handler — validates email uniqueness, hashes password (BCrypt),
 * saves user, then delegates token generation + session creation to AbstractAuthHandler.
 */
@Component
public class RegisterHandler extends AbstractAuthHandler<RegisterRequest> {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public RegisterHandler(RefreshTokenRepository refreshTokenRepository,
                         JwtTokenProvider jwtTokenProvider,
                         JwtProperties jwtProperties,
                         CacheService cacheService,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
    super(refreshTokenRepository, jwtTokenProvider, jwtProperties, cacheService);
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void preHandle(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
  }

  @Override
  @Transactional
  protected User resolveUser(RegisterRequest request) {
    User user = User.builder()
      .email(request.getEmail())
      .passwordHash(passwordEncoder.encode(request.getPassword()))
      .displayName(request.getDisplayName())
      .isActive(true)
      .build();
    return userRepository.save(user);
  }
}
