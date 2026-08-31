package com.personalfinance.auth.features.authen.dto.request;

import com.personalfinance.common.base.request.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Logout request DTO.
 * Contains the userId and sessionId extracted from the JWT token by the controller.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest implements BaseRequest {

  private UUID userId;
  private String sessionId;
}
