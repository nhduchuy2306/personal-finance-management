package com.personalfinance.auth.features.telegram.dto.request;

import com.personalfinance.common.base.request.UserAwareRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Link Telegram request DTO.
 * userId is auto-populated by AbstractController from UserContext.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LinkTelegramRequest implements UserAwareRequest {

  private UUID userId;

  @NotBlank(message = "OTP code is required")
  private String otpCode;
}
