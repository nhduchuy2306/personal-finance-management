package com.personalfinance.auth.features.authen.dto.response;

import com.personalfinance.common.base.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Auth response DTO — returned by register, login, refresh.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse implements BaseResponse {

  private String accessToken;
  private String refreshToken;
  private UUID userId;
  private String email;
  private String displayName;
}
