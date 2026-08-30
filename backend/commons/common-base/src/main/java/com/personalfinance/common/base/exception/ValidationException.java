package com.personalfinance.common.base.exception;

/**
 * Thrown when validation fails.
 */
public class ValidationException extends BusinessException {

  public ValidationException(String message) {
    super(ErrorCode.VALIDATION_ERROR, message);
  }
}
