package com.personalfinance.auth.features.authen.handler.command;

import com.personalfinance.auth.features.authen.dto.request.LogoutRequest;
import com.personalfinance.auth.repository.RefreshTokenRepository;
import com.personalfinance.common.base.handler.AbstractHandler;
import com.personalfinance.common.base.response.VoidResponse;
import com.personalfinance.common.cache.enums.CacheKey;
import com.personalfinance.common.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logout handler — invalidates the user's session by:
 * 1. Deleting session from Redis cache → access tokens with this sessionId become invalid
 * 2. Deleting all refresh tokens for the user from DB → prevents token refresh
 */
@Component
@RequiredArgsConstructor
public class LogoutHandler extends AbstractHandler<LogoutRequest, VoidResponse> {

  private final CacheService cacheService;
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  @Transactional
  public VoidResponse doHandle(LogoutRequest request) {
    // Delete session from Redis → invalidates all access tokens with this sessionId
    cacheService.delete(CacheKey.SESSION.buildKey(request.getSessionId()));

    // Delete all refresh tokens for the user → prevents re-authentication
    refreshTokenRepository.deleteByUserId(request.getUserId());

    return VoidResponse.INSTANCE;
  }
}
