package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.request.RegisterRequest;
import com.personalfinance.auth.features.authen.dto.response.AuthResponse;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.security.jwt.JwtProperties;
import com.personalfinance.common.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RegisterHandler.
 */
@ExtendWith(MockitoExtension.class)
class RegisterHandlerTest {

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

  @InjectMocks
  private RegisterHandler handler;

  private RegisterRequest validRequest;

  @BeforeEach
  void setUp() {
    validRequest = RegisterRequest.builder()
      .email("test@example.com")
      .password("password123")
      .displayName("Test User")
      .build();
  }

  @Test
  @DisplayName("Should throw EMAIL_ALREADY_EXISTS when email is taken")
  void preHandle_emailExists_throwsException() {
    when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

    assertThatThrownBy(() -> handler.preHandle(validRequest))
      .isInstanceOf(BusinessException.class)
      .extracting(e -> ((BusinessException) e).getErrorCode())
      .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("Should pass preHandle when email is unique")
  void preHandle_emailUnique_passes() {
    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

    assertThatCode(() -> handler.preHandle(validRequest)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("Should register user and return JWT pair")
  void doHandle_validRequest_registersUserAndReturnsTokens() {
    UUID userId = UUID.randomUUID();
    User savedUser = User.builder()
      .email("test@example.com")
      .passwordHash("hashed")
      .displayName("Test User")
      .build();
    savedUser.setId(userId);

    when(passwordEncoder.encode("password123")).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtTokenProvider.generateAccessToken(userId, "test@example.com")).thenReturn("access-token");
    when(jwtTokenProvider.generateRefreshToken(userId, "test@example.com")).thenReturn("refresh-token");
    when(jwtProperties.getRefreshTokenExpiry()).thenReturn(604800000L);
    when(refreshTokenRepository.save(any())).thenReturn(null);

    AuthResponse response = handler.doHandle(validRequest);

    assertThat(response.getAccessToken()).isEqualTo("access-token");
    assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    assertThat(response.getUserId()).isEqualTo(userId);
    assertThat(response.getEmail()).isEqualTo("test@example.com");
    assertThat(response.getDisplayName()).isEqualTo("Test User");

    verify(passwordEncoder).encode("password123");
    verify(userRepository).save(any(User.class));
    verify(refreshTokenRepository).save(any());
  }
}
