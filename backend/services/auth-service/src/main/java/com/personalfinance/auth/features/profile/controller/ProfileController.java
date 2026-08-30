package com.personalfinance.auth.features.profile.controller;

import com.personalfinance.auth.features.profile.dto.request.GetProfileRequest;
import com.personalfinance.auth.features.profile.dto.request.UpdateProfileRequest;
import com.personalfinance.auth.features.profile.dto.response.ProfileResponse;
import com.personalfinance.common.base.handler.HandlerRegistry;
import com.personalfinance.common.base.response.ApiResponse;
import com.personalfinance.common.web.controller.AbstractController;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Profile controller — thin dispatcher for user profile endpoints.
 * Extends AbstractController — userId is auto-populated for UserAwareRequest DTOs.
 */
@RestController
@RequestMapping("/api/v1/users/me")
public class ProfileController extends AbstractController {

  public ProfileController(HandlerRegistry registry) {
    super(registry);
  }

  @GetMapping
  public ApiResponse<ProfileResponse> getProfile() {
    // GetProfileRequest implements UserAwareRequest → userId auto-populated by dispatch()
    return dispatch(new GetProfileRequest());
  }

  @PutMapping
  public ApiResponse<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
    // UpdateProfileRequest implements UserAwareRequest → userId auto-populated by dispatch()
    return dispatch(request);
  }
}
