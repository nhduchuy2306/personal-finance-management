package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.request.LoginRequest;
import com.personalfinance.auth.features.authen.dto.response.AuthResponse;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.cache.service.CacheService;
import com.personalfinance.common.security.jwt.JwtProperties;
import com.personalfinance.common.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LoginHandler.
 */
@ExtendWith(MockitoExtension.class)
class LoginHandlerTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private RefreshTokenRepository refreshTokenRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtTokenProvider jwtTokenProvider;
  @Mock
  private JwtProperties jwtProperties;
  @Mock
  private CacheService cacheService;

  @InjectMocks
  private LoginHandler handler;

  @Test
  @DisplayName("Should throw INVALID_CREDENTIALS when email not found")
  void doHandle_emailNotFound_throwsException() {
    LoginRequest request = LoginRequest.builder()
      .email("unknown@example.com")
      .password("password")
      .build();

    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.doHandle(request))
      .isInstanceOf(BusinessException.class)
      .extracting(e -> ((BusinessException) e).getErrorCode())
      .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  @DisplayName("Should throw INVALID_CREDENTIALS when password wrong")
  void doHandle_wrongPassword_throwsException() {
    UUID userId = UUID.randomUUID();
    User user = User.builder()
      .email("test@example.com")
      .passwordHash("hashed-password")
      .displayName("Test")
      .build();
    user.setId(userId);

    LoginRequest request = LoginRequest.builder()
      .email("test@example.com")
      .password("wrong-password")
      .build();

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

    assertThatThrownBy(() -> handler.doHandle(request))
      .isInstanceOf(BusinessException.class)
      .extracting(e -> ((BusinessException) e).getErrorCode())
      .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  @DisplayName("Should login successfully and return JWT pair")
  void doHandle_validCredentials_returnsTokens() {
    UUID userId = UUID.randomUUID();
    User user = User.builder()
      .email("test@example.com")
      .passwordHash("hashed-password")
      .displayName("Test User")
      .build();
    user.setId(userId);

    LoginRequest request = LoginRequest.builder()
      .email("test@example.com")
      .password("correct-password")
      .build();

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
    when(jwtTokenProvider.generateAccessToken(userId, "test@example.com")).thenReturn("access-token");
    when(jwtTokenProvider.generateRefreshToken(userId, "test@example.com")).thenReturn("refresh-token");
    when(jwtProperties.getRefreshTokenExpiry()).thenReturn(604800000L);
    when(refreshTokenRepository.save(any())).thenReturn(null);

    AuthResponse response = handler.doHandle(request);

    assertThat(response.getAccessToken()).isEqualTo("access-token");
    assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    assertThat(response.getUserId()).isEqualTo(userId);
    assertThat(response.getEmail()).isEqualTo("test@example.com");
  }
}
