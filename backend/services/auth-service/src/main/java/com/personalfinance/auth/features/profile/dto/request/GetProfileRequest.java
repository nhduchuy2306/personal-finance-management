package com.personalfinance.auth.features.profile.dto.request;

import com.personalfinance.common.base.request.UserAwareRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Get profile request — userId is auto-populated by AbstractController from UserContext.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GetProfileRequest implements UserAwareRequest {

  private UUID userId;
}
