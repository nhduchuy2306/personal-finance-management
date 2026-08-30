package com.personalfinance.auth.features.authen.dto.response;

import com.personalfinance.common.base.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Auth response DTO — returned by register, login, refresh.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse implements BaseResponse {

  private String accessToken;
  private String refreshToken;
  private UUID userId;
  private String email;
  private String displayName;
}
