package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.response.AuthResponse;
import com.personalfinance.auth.model.RefreshToken;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.common.base.handler.AbstractHandler;
import com.personalfinance.common.base.request.BaseRequest;
import com.personalfinance.common.security.jwt.JwtProperties;
import com.personalfinance.common.security.jwt.JwtTokenProvider;

import java.time.LocalDateTime;

/**
 * Abstract base for authentication handlers that produce JWT pairs.
 * Extracts the shared logic: generate tokens, save refresh token, build AuthResponse.
 *
 * <p>Subclasses only need to implement {@link #resolveUser(BaseRequest)} to provide
 * the authenticated User — the token generation and response building are handled here.
 *
 * @param <Req> Request DTO type
 */
public abstract class AbstractAuthHandler<Req extends BaseRequest>
  extends AbstractHandler<Req, AuthResponse> {

  protected final RefreshTokenRepository refreshTokenRepository;
  protected final JwtTokenProvider jwtTokenProvider;
  protected final JwtProperties jwtProperties;

  protected AbstractAuthHandler(RefreshTokenRepository refreshTokenRepository,
                                JwtTokenProvider jwtTokenProvider,
                                JwtProperties jwtProperties) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtTokenProvider = jwtTokenProvider;
    this.jwtProperties = jwtProperties;
  }

  /**
   * Resolve the authenticated user from the request.
   * LoginHandler: verify email+password → return User.
   * RefreshTokenHandler: validate token, rotate → return User.
   */
  protected abstract User resolveUser(Req request);

  /**
   * Hook called before generating tokens — subclasses can override
   * to perform pre-token actions (e.g., delete old refresh token during rotation).
   */
  protected void beforeTokenGeneration(Req request, User user) {
    // default no-op
  }

  @Override
  public AuthResponse doHandle(Req request) {
    User user = resolveUser(request);

    beforeTokenGeneration(request, user);

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
