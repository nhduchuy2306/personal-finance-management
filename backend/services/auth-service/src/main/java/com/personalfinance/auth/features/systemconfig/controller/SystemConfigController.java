package com.personalfinance.auth.features.systemconfig.controller;

import com.personalfinance.auth.features.systemconfig.dto.request.WarmUpSystemConfigRequest;
import com.personalfinance.auth.features.systemconfig.dto.response.WarmUpSystemConfigResponse;
import com.personalfinance.common.base.handler.HandlerRegistry;
import com.personalfinance.common.base.response.ApiResponse;
import com.personalfinance.common.web.controller.AbstractController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * System config controller — admin endpoints for managing dynamic system configuration.
 * Requires authentication (admin access).
 */
@RestController
@RequestMapping("/api/v1/system-config")
public class SystemConfigController extends AbstractController {

  public SystemConfigController(HandlerRegistry registry) {
    super(registry);
  }

  /**
   * Manually trigger cache warm-up for all system config values.
   * Loads all configs from DB → sets Redis with 1-day TTL.
   * Use when: Redis was flushed, config values were updated directly in DB,
   * or you want to ensure Redis is in sync.
   */
  @PostMapping("/warm-up")
  public ApiResponse<WarmUpSystemConfigResponse> warmUp() {
    return dispatch(WarmUpSystemConfigRequest.builder().build());
  }
}
