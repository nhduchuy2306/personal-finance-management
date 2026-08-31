package com.personalfinance.auth.features.authen.controller;

import com.personalfinance.auth.features.authen.dto.request.LoginRequest;
import com.personalfinance.auth.features.authen.dto.request.LogoutRequest;
import com.personalfinance.auth.features.authen.dto.request.RefreshTokenRequest;
import com.personalfinance.auth.features.authen.dto.request.RegisterRequest;
import com.personalfinance.auth.features.authen.dto.response.AuthResponse;
import com.personalfinance.common.base.handler.HandlerRegistry;
import com.personalfinance.common.base.response.ApiResponse;
import com.personalfinance.common.base.response.VoidResponse;
import com.personalfinance.common.security.context.UserContext;
import com.personalfinance.common.security.jwt.JwtTokenValidator;
import com.personalfinance.common.web.controller.AbstractController;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth controller — thin dispatcher for authentication endpoints.
 * Extends AbstractController to leverage centralized dispatch.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends AbstractController {

  @Getter
  private final JwtTokenValidator jwtTokenValidator;

  public AuthController(HandlerRegistry registry, JwtTokenValidator jwtTokenValidator) {
    super(registry);
    this.jwtTokenValidator = jwtTokenValidator;
  }

  @PostMapping("/register")
  public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return dispatch(request);
  }

  @PostMapping("/login")
  public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return dispatch(request);
  }

  @PostMapping("/refresh")
  public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return dispatch(request);
  }

  @PostMapping("/logout")
  public ApiResponse<VoidResponse> logout(@RequestHeader("Authorization") String bearerToken) {
    String token = bearerToken.substring(7);
    String sessionId = jwtTokenValidator.extractSessionId(token);

    LogoutRequest request = LogoutRequest.builder()
      .userId(UserContext.getCurrentUserId())
      .sessionId(sessionId)
      .build();

    return dispatch(request);
  }
}
