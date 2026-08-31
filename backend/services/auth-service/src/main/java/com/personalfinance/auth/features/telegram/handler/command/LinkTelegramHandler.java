package com.personalfinance.auth.features.telegram.handler.command;

import com.personalfinance.auth.features.profile.dto.response.ProfileResponse;
import com.personalfinance.auth.features.telegram.dto.request.LinkTelegramRequest;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.base.handler.AbstractHandler;
import com.personalfinance.common.cache.enums.CacheKey;
import com.personalfinance.common.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Link Telegram handler — validates OTP from Redis (telegram:otp:{code}, TTL 5min),
 * saves chat_id to user, deletes OTP.
 * Cache eviction is handled automatically by CacheAwareRepository + UserCacheEvictionRule.
 */
@Component
@RequiredArgsConstructor
public class LinkTelegramHandler extends AbstractHandler<LinkTelegramRequest, ProfileResponse> {

  private final UserRepository userRepository;
  private final CacheService cacheService;

  @Override
  public void preHandle(LinkTelegramRequest request) {
    // Validate OTP exists in Redis
    String otpKey = CacheKey.TELEGRAM_OTP.buildKey(request.getOtpCode());
    if (!cacheService.exists(otpKey)) {
      throw new BusinessException(ErrorCode.OTP_INVALID);
    }
  }

  @Override
  @Transactional
  public ProfileResponse doHandle(LinkTelegramRequest request) {
    // Get chat_id from OTP value in Redis
    String otpKey = CacheKey.TELEGRAM_OTP.buildKey(request.getOtpCode());
    String chatId = cacheService.get(otpKey, String.class)
      .orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));

    // Find user and save chat_id
    User user = userRepository.findById(request.getUserId())
      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    user.setTelegramChatId(chatId);
    user = userRepository.save(user);

    // Delete OTP from Redis (one-time use)
    cacheService.delete(otpKey);

    return ProfileResponse.from(user);
  }
}
