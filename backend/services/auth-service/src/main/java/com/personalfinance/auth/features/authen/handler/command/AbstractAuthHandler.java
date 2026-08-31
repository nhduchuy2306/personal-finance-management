package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.response.AuthResponse;
import com.personalfinance.auth.model.RefreshToken;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.common.base.handler.AbstractHandler;
import com.personalfinance.common.base.request.BaseRequest;
import com.personalfinance.common.cache.enums.CacheKey;
import com.personalfinance.common.cache.service.CacheService;
import com.personalfinance.common.security.jwt.JwtProperties;
import com.personalfinance.common.security.jwt.JwtTokenProvider;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract base for authentication handlers that produce JWT pairs.
 * Extracts the shared logic: generate sessionId, create tokens, save refresh token,
 * cache session, and build AuthResponse.
 *
 * <p>Subclasses only need to implement {@link #resolveUser(BaseRequest)} to provide
 * the authenticated User — the token generation and response building are handled here.
 *
 * <p>Session flow:
 * <ul>
 *   <li>New login/register → generate new sessionId</li>
 *   <li>Token refresh → reuse existing sessionId via {@link #resolveSessionId(BaseRequest)}</li>
 *   <li>Logout → delete session from Redis (handled by LogoutHandler)</li>
 * </ul>
 *
 * @param <Req> Request DTO type
 */
public abstract class AbstractAuthHandler<Req extends BaseRequest>
  extends AbstractHandler<Req, AuthResponse> {

  protected final RefreshTokenRepository refreshTokenRepository;
  protected final JwtTokenProvider jwtTokenProvider;
  protected final JwtProperties jwtProperties;
  protected final CacheService cacheService;

  protected AbstractAuthHandler(RefreshTokenRepository refreshTokenRepository,
                                JwtTokenProvider jwtTokenProvider,
                                JwtProperties jwtProperties,
                                CacheService cacheService) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtTokenProvider = jwtTokenProvider;
    this.jwtProperties = jwtProperties;
    this.cacheService = cacheService;
  }

  /**
   * Resolve the authenticated user from the request.
   * LoginHandler: verify email+password → return User.
   * RegisterHandler: create new User → return User.
   * RefreshTokenHandler: validate token, rotate → return User.
   */
  protected abstract User resolveUser(Req request);

  /**
   * Resolve the sessionId to use. Default: generate a new one.
   * RefreshTokenHandler overrides this to reuse the existing sessionId.
   */
  protected String resolveSessionId(Req request) {
    return UUID.randomUUID().toString();
  }

  /**
   * Hook called before generating tokens — subclasses can override
   * to perform pre-token actions (e.g., delete old refresh token during rotation).
   */
  protected void beforeTokenGeneration(Req request, User user) {
    // default no-op
  }

  @Override
  @Transactional
  public AuthResponse doHandle(Req request) {
    User user = resolveUser(request);

    beforeTokenGeneration(request, user);

    String sessionId = resolveSessionId(request);

    // Generate JWT pair — sessionId embedded in access token only
    String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), sessionId);
    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

    // Save refresh token with sessionId for reuse during token refresh
    RefreshToken tokenEntity = RefreshToken.builder()
      .userId(user.getId())
      .token(refreshToken)
      .sessionId(sessionId)
      .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiry() / 1000))
      .build();
    refreshTokenRepository.save(tokenEntity);

    // Cache session — TTL matches refresh token expiry (7 days)
    cacheService.set(
      CacheKey.SESSION.buildKey(sessionId),
      user.getId().toString(),
      Duration.ofMillis(jwtProperties.getRefreshTokenExpiry())
    );

    return AuthResponse.builder()
      .accessToken(accessToken)
      .refreshToken(refreshToken)
      .userId(user.getId())
      .email(user.getEmail())
      .displayName(user.getDisplayName())
      .build();
  }
}
