package com.personalfinance.common.base.exception;

/**
 * Thrown when no handler is registered for a request type.
 */
public class HandlerNotFoundException extends BusinessException {

    public HandlerNotFoundException(String message) {
        super(ErrorCode.HANDLER_NOT_FOUND, message);
    }
}
