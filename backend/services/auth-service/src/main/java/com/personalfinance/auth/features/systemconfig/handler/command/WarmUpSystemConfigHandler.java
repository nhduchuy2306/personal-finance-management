package com.personalfinance.auth.features.systemconfig.handler.command;

import com.personalfinance.auth.features.systemconfig.dto.request.WarmUpSystemConfigRequest;
import com.personalfinance.auth.features.systemconfig.dto.response.WarmUpSystemConfigResponse;
import com.personalfinance.auth.features.systemconfig.service.SystemConfigCacheWarmer;
import com.personalfinance.common.base.handler.AbstractHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for manual system config cache warm-up.
 * Called via admin API to force re-populate Redis with all config values.
 */
@Component
@RequiredArgsConstructor
public class WarmUpSystemConfigHandler
  extends AbstractHandler<WarmUpSystemConfigRequest, WarmUpSystemConfigResponse> {

  private final SystemConfigCacheWarmer cacheWarmer;

  @Override
  public WarmUpSystemConfigResponse doHandle(WarmUpSystemConfigRequest request) {
    int count = cacheWarmer.warmAll();
    return WarmUpSystemConfigResponse.builder()
      .configsWarmed(count)
      .message("System config cache warmed successfully")
      .build();
  }
}
