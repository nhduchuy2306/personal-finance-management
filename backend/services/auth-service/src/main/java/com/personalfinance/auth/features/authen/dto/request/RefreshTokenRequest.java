package com.personalfinance.auth.features.authen.dto.request;

import com.personalfinance.common.base.request.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Refresh token request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest implements BaseRequest {

  @NotBlank(message = "Refresh token is required")
  private String refreshToken;
}
