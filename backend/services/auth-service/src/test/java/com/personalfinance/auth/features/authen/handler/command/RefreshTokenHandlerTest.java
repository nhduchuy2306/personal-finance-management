package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.request.RefreshTokenRequest;
import com.personalfinance.auth.features.authen.dto.response.AuthResponse;
import com.personalfinance.auth.model.RefreshToken;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.security.jwt.JwtProperties;
import com.personalfinance.common.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RefreshTokenHandler.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenHandlerTest {

  @Mock
  private RefreshTokenRepository refreshTokenRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private JwtTokenProvider jwtTokenProvider;
  @Mock
  private JwtProperties jwtProperties;

  @InjectMocks
  private RefreshTokenHandler handler;

  @Test
  @DisplayName("Should throw TOKEN_INVALID when token not found")
  void doHandle_tokenNotFound_throwsException() {
    RefreshTokenRequest request = RefreshTokenRequest.builder()
      .refreshToken("non-existent-token")
      .build();

    when(refreshTokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handler.doHandle(request))
      .isInstanceOf(BusinessException.class)
      .extracting(e -> ((BusinessException) e).getErrorCode())
      .isEqualTo(ErrorCode.TOKEN_INVALID);
  }

  @Test
  @DisplayName("Should throw REFRESH_TOKEN_EXPIRED when token expired")
  void doHandle_tokenExpired_throwsException() {
    UUID userId = UUID.randomUUID();
    RefreshToken expiredToken = RefreshToken.builder()
      .userId(userId)
      .token("expired-token")
      .expiresAt(LocalDateTime.now().minusHours(1))
      .build();

    RefreshTokenRequest request = RefreshTokenRequest.builder()
      .refreshToken("expired-token")
      .build();

    when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

    assertThatThrownBy(() -> handler.doHandle(request))
      .isInstanceOf(BusinessException.class)
      .extracting(e -> ((BusinessException) e).getErrorCode())
      .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED);

    verify(refreshTokenRepository).delete(expiredToken);
  }

  @Test
  @DisplayName("Should rotate token and return new JWT pair")
  void doHandle_validToken_rotatesAndReturnsNewPair() {
    UUID userId = UUID.randomUUID();
    RefreshToken validToken = RefreshToken.builder()
      .userId(userId)
      .token("old-refresh-token")
      .expiresAt(LocalDateTime.now().plusDays(7))
      .build();

    User user = User.builder()
      .email("test@example.com")
      .displayName("Test User")
      .passwordHash("hash")
      .build();
    user.setId(userId);

    RefreshTokenRequest request = RefreshTokenRequest.builder()
      .refreshToken("old-refresh-token")
      .build();

    when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(validToken));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(jwtTokenProvider.generateAccessToken(userId, "test@example.com")).thenReturn("new-access-token");
    when(jwtTokenProvider.generateRefreshToken(userId, "test@example.com")).thenReturn("new-refresh-token");
    when(jwtProperties.getRefreshTokenExpiry()).thenReturn(604800000L);
    when(refreshTokenRepository.save(any())).thenReturn(null);

    AuthResponse response = handler.doHandle(request);

    assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");

    // Verify old token was deleted (rotation)
    verify(refreshTokenRepository).delete(validToken);
    // Verify new token was saved
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }
}
