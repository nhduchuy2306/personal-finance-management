package com.personalfinance.auth.features.systemconfig.dto.response;

import com.personalfinance.common.base.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Response DTO for system config cache warm-up.
 * Returns the number of config entries warmed into Redis.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarmUpSystemConfigResponse implements BaseResponse {

  private int configsWarmed;
  private String message;
}
