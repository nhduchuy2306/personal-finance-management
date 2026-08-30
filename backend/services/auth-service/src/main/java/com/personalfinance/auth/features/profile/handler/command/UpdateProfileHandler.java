package com.personalfinance.auth.features.profile.handler.command;

import com.personalfinance.auth.features.profile.dto.request.UpdateProfileRequest;
import com.personalfinance.auth.features.profile.dto.response.ProfileResponse;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.base.handler.AbstractHandler;
import com.personalfinance.common.cache.key.CacheKeyBuilder;
import com.personalfinance.common.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Update profile handler — updates user in DB, invalidates cache in postHandle.
 */
@Component
@RequiredArgsConstructor
public class UpdateProfileHandler extends AbstractHandler<UpdateProfileRequest, ProfileResponse> {

  private final UserRepository userRepository;
  private final CacheService cacheService;

  @Override
  @Transactional
  public ProfileResponse doHandle(UpdateProfileRequest request) {
    User user = userRepository.findById(request.getUserId())
      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // Update fields if provided
    if (request.getDisplayName() != null) {
      user.setDisplayName(request.getDisplayName());
    }
    if (request.getAvatarUrl() != null) {
      user.setAvatarUrl(request.getAvatarUrl());
    }

    user = userRepository.save(user);
    return ProfileResponse.from(user);
  }

  @Override
  public void postHandle(UpdateProfileRequest request, ProfileResponse response) {
    // Invalidate user profile cache
    cacheService.delete(CacheKeyBuilder.userProfile(request.getUserId()));
  }
}
