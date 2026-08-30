package com.personalfinance.auth.features.telegram.dto.request;

import com.personalfinance.common.base.request.UserAwareRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Unlink Telegram request DTO.
 * userId is auto-populated by AbstractController from UserContext.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UnlinkTelegramRequest implements UserAwareRequest {

  private UUID userId;
}
