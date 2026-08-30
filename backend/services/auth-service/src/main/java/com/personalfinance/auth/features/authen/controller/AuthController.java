package com.personalfinance.auth.features.authen.controller;

import com.personalfinance.auth.features.authen.dto.request.LoginRequest;
import com.personalfinance.auth.features.authen.dto.request.RefreshTokenRequest;
import com.personalfinance.auth.features.authen.dto.request.RegisterRequest;
import com.personalfinance.auth.features.authen.dto.response.AuthResponse;
import com.personalfinance.common.base.handler.HandlerRegistry;
import com.personalfinance.common.base.response.ApiResponse;
import com.personalfinance.common.web.controller.AbstractController;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth controller — thin dispatcher for authentication endpoints.
 * Extends AbstractController to leverage centralized dispatch.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends AbstractController {

  public AuthController(HandlerRegistry registry) {
    super(registry);
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
}
