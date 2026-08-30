package com.personalfinance.common.base.exception;

import lombok.Getter;

/**
 * Error codes for the application.
 * Each code has a unique string identifier and default message.
 */
@Getter
public enum ErrorCode {

  // Common
  INTERNAL_ERROR("COMMON_001", "Internal server error"),
  VALIDATION_ERROR("COMMON_002", "Validation failed"),
  RESOURCE_NOT_FOUND("COMMON_003", "Resource not found"),
  HANDLER_NOT_FOUND("COMMON_004", "No handler registered for this request"),
  UNAUTHORIZED("COMMON_005", "Unauthorized"),
  FORBIDDEN("COMMON_006", "Access denied"),
  DUPLICATE_RESOURCE("COMMON_007", "Resource already exists"),

  // Auth
  INVALID_CREDENTIALS("AUTH_001", "Invalid email or password"),
  TOKEN_EXPIRED("AUTH_002", "Token has expired"),
  TOKEN_INVALID("AUTH_003", "Invalid token"),
  EMAIL_ALREADY_EXISTS("AUTH_004", "Email already registered"),
  USER_NOT_FOUND("AUTH_005", "User not found"),
  REFRESH_TOKEN_EXPIRED("AUTH_006", "Refresh token has expired"),
  TELEGRAM_ALREADY_LINKED("AUTH_007", "Telegram already linked to another account"),
  OTP_INVALID("AUTH_008", "Invalid or expired OTP"),

  // Budget
  BUDGET_PERIOD_NOT_FOUND("BUDGET_001", "Budget period not found"),
  BUDGET_ALREADY_ACTIVE("BUDGET_002", "An active budget period already exists"),
  ALLOCATION_EXCEEDS_BUDGET("BUDGET_003", "Total allocations exceed monthly budget"),
  CATEGORY_NOT_FOUND("BUDGET_004", "Category not found"),
  DRAFT_NOT_FOUND("BUDGET_005", "No draft budget found"),

  // Transaction
  TRANSACTION_NOT_FOUND("TXN_001", "Transaction not found"),
  INVALID_AMOUNT("TXN_002", "Amount must be positive"),
  NO_ACTIVE_BUDGET("TXN_003", "No active budget period"),

  // OCR
  RECEIPT_NOT_FOUND("OCR_001", "Receipt not found"),
  OCR_PROCESSING_FAILED("OCR_002", "OCR processing failed"),
  RECEIPT_ALREADY_CONFIRMED("OCR_003", "Receipt already confirmed"),
  IMAGE_UPLOAD_FAILED("OCR_004", "Failed to upload image"),

  // Group Expense
  GROUP_NOT_FOUND("GROUP_001", "Group not found"),
  MEMBER_NOT_FOUND("GROUP_002", "Member not found"),
  SPLIT_AMOUNT_MISMATCH("GROUP_003", "Split amounts do not equal total"),
  MEMBER_ALREADY_EXISTS("GROUP_004", "Member already in group"),
  SETTLEMENT_EXCEEDS_DEBT("GROUP_005", "Settlement amount exceeds outstanding debt"),

  // Notification
  TELEGRAM_NOT_LINKED("NOTIF_001", "User has not linked Telegram"),
  NOTIFICATION_SEND_FAILED("NOTIF_002", "Failed to send notification"),

  // Recurring Bill
  BILL_NOT_FOUND("BILL_001", "Recurring bill not found"),
  BILL_ALREADY_PAID("BILL_002", "Bill already paid for this cycle"),

  // Saving
  GOAL_NOT_FOUND("SAVING_001", "Savings goal not found"),
  GOAL_ALREADY_COMPLETED("SAVING_002", "Savings goal already completed"),
  GOAL_CANCELLED("SAVING_003", "Savings goal has been cancelled"),
  CONTRIBUTION_EXCEEDS_REMAINING("SAVING_004", "Contribution exceeds remaining target");

  private final String code;
  private final String message;

  ErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }
}
