package com.personalfinance.auth.features.profile.handler.query;

import com.personalfinance.auth.features.profile.dto.request.GetProfileRequest;
import com.personalfinance.auth.features.profile.dto.response.ProfileResponse;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.base.handler.AbstractHandler;
import com.personalfinance.common.cache.key.CacheKeyBuilder;
import com.personalfinance.common.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Get profile handler — reads from Redis cache first (user:{id}, TTL 30min),
 * falls back to DB on cache miss.
 */
@Component
@RequiredArgsConstructor
public class GetProfileHandler extends AbstractHandler<GetProfileRequest, ProfileResponse> {

  private static final Duration CACHE_TTL = Duration.ofMinutes(30);
  private final UserRepository userRepository;
  private final CacheService cacheService;

  @Override
  public ProfileResponse doHandle(GetProfileRequest request) {
    String cacheKey = CacheKeyBuilder.userProfile(request.getUserId());

    // Try cache first
    return cacheService.get(cacheKey, ProfileResponse.class)
      .orElseGet(() -> {
        // Fallback to DB
        ProfileResponse profile = userRepository.findById(request.getUserId())
          .map(ProfileResponse::from)
          .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Cache the result
        cacheService.set(cacheKey, profile, CACHE_TTL);
        return profile;
      });
  }
}
