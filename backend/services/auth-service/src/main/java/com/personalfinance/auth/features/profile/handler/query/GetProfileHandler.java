package com.personalfinance.auth.features.profile.handler.query;

import com.personalfinance.auth.features.profile.dto.request.GetProfileRequest;
import com.personalfinance.auth.features.profile.dto.response.ProfileResponse;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.base.handler.AbstractHandler;
import com.personalfinance.common.cache.enums.CacheKey;
import com.personalfinance.common.cache.enums.ConfigName;
import com.personalfinance.common.cache.service.CacheService;
import com.personalfinance.common.cache.systemconfig.SystemConfigReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Get profile handler — reads from Redis cache first (user:{id}, TTL from system config),
 * falls back to DB on cache miss.
 */
@Component
@RequiredArgsConstructor
public class GetProfileHandler extends AbstractHandler<GetProfileRequest, ProfileResponse> {

  private final UserRepository userRepository;
  private final CacheService cacheService;
  private final SystemConfigReader systemConfigReader;

  @Override
  public ProfileResponse doHandle(GetProfileRequest request) {
    String cacheKey = CacheKey.USER_PROFILE.buildKey(request.getUserId());

    // Try cache first
    return cacheService.get(cacheKey, ProfileResponse.class)
      .orElseGet(() -> {
        // Fallback to DB
        ProfileResponse profile = userRepository.findById(request.getUserId())
          .map(ProfileResponse::from)
          .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Cache the result — TTL from system config
        cacheService.set(cacheKey, profile,
          systemConfigReader.getAsDuration(ConfigName.CACHE_TTL_USER_PROFILE));
        return profile;
      });
  }
}

