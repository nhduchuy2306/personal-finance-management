package com.personalfinance.common.base.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base Handler with template method pattern.
 * Subclasses override preHandle/doHandle/postHandle.
 * The execute() method orchestrates the full flow.
 */
@Slf4j
public abstract class AbstractHandler<Req, Res> implements Handler<Req, Res> {

    /**
     * Template method — orchestrates the full handler flow.
     * Call this from controllers/consumers, NOT doHandle directly.
     */
    public Res execute(Req request) {
        preHandle(request);
        Res response = doHandle(request);
        try {
            postHandle(request, response);
        } catch (Exception e) {
            // postHandle failures are logged but don't rollback the main operation
            log.error("postHandle failed for {}: {}", getRequestType().getSimpleName(), e.getMessage(), e);
        }
        return response;
    }

    @Override
    public void preHandle(Req request) {
        // default no-op — override when needed
    }

    @Override
    public void postHandle(Req request, Res response) {
        // default no-op — override when needed
    }
}
