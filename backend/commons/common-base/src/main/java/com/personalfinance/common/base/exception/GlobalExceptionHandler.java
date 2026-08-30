package com.personalfinance.common.base.exception;

import com.personalfinance.common.base.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler via @ControllerAdvice.
 * Converts exceptions to standardized ApiResponse format.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
    log.warn("Business exception: [{}] {}", ex.getErrorCode().getCode(), ex.getMessage());
    return ResponseEntity
      .status(mapToHttpStatus(ex.getErrorCode()))
      .body(ApiResponse.error(ex.getErrorCode().getCode(), ex.getMessage()));
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex) {
    log.warn("Validation exception: {}", ex.getMessage());
    return ResponseEntity
      .badRequest()
      .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ApiResponse.error(ex.getErrorCode().getCode(), ex.getMessage()));
  }

  @ExceptionHandler(HandlerNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleHandlerNotFound(HandlerNotFoundException ex) {
    log.error("Handler not found: {}", ex.getMessage());
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiResponse.error(ErrorCode.HANDLER_NOT_FOUND.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    String details = ex.getBindingResult().getFieldErrors().stream()
      .map(error -> error.getField() + ": " + error.getDefaultMessage())
      .collect(Collectors.joining(", "));
    log.warn("Validation failed: {}", details);
    return ResponseEntity
      .badRequest()
      .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.getCode(), details));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "An unexpected error occurred"));
  }

  private HttpStatus mapToHttpStatus(ErrorCode errorCode) {
    return switch (errorCode) {
      case RESOURCE_NOT_FOUND, USER_NOT_FOUND, BUDGET_PERIOD_NOT_FOUND,
           CATEGORY_NOT_FOUND, DRAFT_NOT_FOUND, TRANSACTION_NOT_FOUND,
           RECEIPT_NOT_FOUND, GROUP_NOT_FOUND, MEMBER_NOT_FOUND,
           BILL_NOT_FOUND, GOAL_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case UNAUTHORIZED, INVALID_CREDENTIALS, TOKEN_EXPIRED, TOKEN_INVALID,
           REFRESH_TOKEN_EXPIRED -> HttpStatus.UNAUTHORIZED;
      case FORBIDDEN -> HttpStatus.FORBIDDEN;
      case DUPLICATE_RESOURCE, EMAIL_ALREADY_EXISTS, TELEGRAM_ALREADY_LINKED,
           BUDGET_ALREADY_ACTIVE, MEMBER_ALREADY_EXISTS -> HttpStatus.CONFLICT;
      case VALIDATION_ERROR, INVALID_AMOUNT, ALLOCATION_EXCEEDS_BUDGET,
           SPLIT_AMOUNT_MISMATCH, OTP_INVALID, SETTLEMENT_EXCEEDS_DEBT,
           CONTRIBUTION_EXCEEDS_REMAINING -> HttpStatus.BAD_REQUEST;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }
}
