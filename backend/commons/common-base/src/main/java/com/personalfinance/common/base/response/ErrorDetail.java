package com.personalfinance.common.base.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Error detail within API response.
 */
@Getter
@Setter
@Builder(toBuilder = true)
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
