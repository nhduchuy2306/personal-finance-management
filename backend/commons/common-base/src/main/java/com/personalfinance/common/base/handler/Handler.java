package com.personalfinance.common.base.handler;

import com.personalfinance.common.base.request.BaseRequest;
import com.personalfinance.common.base.response.BaseResponse;

/**
 * Base Handler interface for CQRS pattern.
 * Each use case = 1 Handler implementation.
 *
 * @param <Req> Request DTO type — must implement {@link BaseRequest}
 * @param <Res> Response DTO type — must implement {@link BaseResponse}
 */
public interface Handler<Req extends BaseRequest, Res extends BaseResponse> {

  /**
   * Validation, data enrichment, permission checks.
   * Throw BusinessException here to abort early.
   */
  void preHandle(Req request);

  /**
   * Core business logic. DB writes, calculations.
   * This is where @Transactional goes.
   */
  Res doHandle(Req request);

  /**
   * Side effects AFTER success: publish Kafka events, invalidate cache.
   * Must not throw — failures here are logged, not rolled back.
   */
  void postHandle(Req request, Res response);

  /**
   * Used by HandlerRegistry to build the dispatch map.
   * Return the Class of the request DTO.
   */
  Class<Req> getRequestType();
}
