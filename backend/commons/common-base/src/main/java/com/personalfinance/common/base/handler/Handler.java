package com.personalfinance.common.base.handler;

/**
 * Base Handler interface for CQRS pattern.
 * Each use case = 1 Handler implementation.
 *
 * @param <Req> Request DTO type
 * @param <Res> Response DTO type
 */
public interface Handler<Req, Res> {

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
