package com.personalfinance.common.base.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error detail within API response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {

  private String code;
  private String message;

  public static ErrorDetail of(String code, String message) {
    return ErrorDetail.builder()
      .code(code)
      .message(message)
      .build();
  }
}
