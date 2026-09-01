package com.personalfinance.auth.features.systemconfig.dto.request;

import com.personalfinance.common.base.request.BaseRequest;
import lombok.Builder;
import lombok.Getter;

/**
 * Request DTO for system config cache warm-up.
 * No fields needed — warming loads ALL configs.
 */
@Getter
@Builder
public class WarmUpSystemConfigRequest implements BaseRequest {
}
