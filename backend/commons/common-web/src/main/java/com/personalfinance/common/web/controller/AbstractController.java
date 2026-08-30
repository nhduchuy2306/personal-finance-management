package com.personalfinance.common.web.controller;

import com.personalfinance.common.base.handler.HandlerRegistry;
import com.personalfinance.common.base.request.BaseRequest;
import com.personalfinance.common.base.request.UserAwareRequest;
import com.personalfinance.common.base.response.ApiResponse;
import com.personalfinance.common.base.response.BaseResponse;
import com.personalfinance.common.security.context.UserContext;

/**
 * Abstract base controller for all REST controllers.
 */
public abstract class AbstractController {

  private final HandlerRegistry registry;

  protected AbstractController(HandlerRegistry registry) {
    this.registry = registry;
  }

  /**
   * Dispatch a request to the HandlerRegistry.
   *
   * @param request the request DTO (must implement BaseRequest)
   * @return ApiResponse wrapping the handler's response
   */
  protected <Res extends BaseResponse> ApiResponse<Res> dispatch(BaseRequest request) {
    enrichRequest(request);
    Res result = registry.dispatch(request);
    return ApiResponse.success(result);
  }

  /**
   * Enrich the request before dispatching.
   * Default behavior: populate userId for UserAwareRequest.
   * Override for additional enrichment (e.g., tenant ID, locale).
   */
  protected void enrichRequest(BaseRequest request) {
    if (request instanceof UserAwareRequest userAwareReq && userAwareReq.getUserId() == null) {
      userAwareReq.setUserId(UserContext.getCurrentUserId());
    }
  }
}
