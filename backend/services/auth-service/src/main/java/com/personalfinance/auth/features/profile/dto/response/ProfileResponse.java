package com.personalfinance.auth.features.profile.dto.response;

import com.personalfinance.auth.model.User;
import com.personalfinance.common.base.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Profile response DTO.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse implements BaseResponse {

  private UUID id;
  private String email;
  private String displayName;
  private String avatarUrl;
  private String telegramChatId;
  private Boolean isActive;
  private LocalDateTime createdAt;

  public static ProfileResponse from(User user) {
    return ProfileResponse.builder()
      .id(user.getId())
      .email(user.getEmail())
      .displayName(user.getDisplayName())
      .avatarUrl(user.getAvatarUrl())
      .telegramChatId(user.getTelegramChatId())
      .isActive(user.getIsActive())
      .createdAt(user.getCreatedAt())
      .build();
  }
}
