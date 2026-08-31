package com.personalfinance.auth.features.telegram.handler.command;

import com.personalfinance.auth.features.profile.dto.response.ProfileResponse;
import com.personalfinance.auth.features.telegram.dto.request.UnlinkTelegramRequest;
import com.personalfinance.auth.model.User;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.base.handler.AbstractHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unlink Telegram handler — sets telegram_chat_id = null.
 * Cache eviction is handled automatically by CacheAwareRepository + UserCacheEvictionRule.
 */
@Component
@RequiredArgsConstructor
public class UnlinkTelegramHandler extends AbstractHandler<UnlinkTelegramRequest, ProfileResponse> {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public ProfileResponse doHandle(UnlinkTelegramRequest request) {
    User user = userRepository.findById(request.getUserId())
      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    user.setTelegramChatId(null);
    user = userRepository.save(user);

    return ProfileResponse.from(user);
  }
}
