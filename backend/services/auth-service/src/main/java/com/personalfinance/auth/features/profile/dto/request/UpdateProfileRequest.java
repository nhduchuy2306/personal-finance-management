package com.personalfinance.auth.features.profile.dto.request;

import com.personalfinance.common.base.request.UserAwareRequest;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Update profile request DTO.
 * userId is auto-populated by AbstractController from UserContext.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest implements UserAwareRequest {

  private UUID userId;

  @Size(min = 1, max = 100, message = "Display name must be between 1 and 100 characters")
  private String displayName;

  @Size(max = 500, message = "Avatar URL must be at most 500 characters")
  private String avatarUrl;
}
