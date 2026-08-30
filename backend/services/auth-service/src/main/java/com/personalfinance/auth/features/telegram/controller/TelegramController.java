package com.personalfinance.auth.features.telegram.controller;

import com.personalfinance.auth.features.profile.dto.response.ProfileResponse;
import com.personalfinance.auth.features.telegram.dto.request.LinkTelegramRequest;
import com.personalfinance.auth.features.telegram.dto.request.UnlinkTelegramRequest;
import com.personalfinance.common.base.handler.HandlerRegistry;
import com.personalfinance.common.base.response.ApiResponse;
import com.personalfinance.common.web.controller.AbstractController;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Telegram controller — thin dispatcher for Telegram link/unlink endpoints.
 * Extends AbstractController — userId is auto-populated for UserAwareRequest DTOs.
 */
@RestController
@RequestMapping("/api/v1/users/me/telegram")
public class TelegramController extends AbstractController {

  public TelegramController(HandlerRegistry registry) {
    super(registry);
  }

  @PostMapping("/link")
  public ApiResponse<ProfileResponse> linkTelegram(@Valid @RequestBody LinkTelegramRequest request) {
    // LinkTelegramRequest implements UserAwareRequest → userId auto-populated
    return dispatch(request);
  }

  @DeleteMapping("/link")
  public ApiResponse<ProfileResponse> unlinkTelegram() {
    // UnlinkTelegramRequest implements UserAwareRequest → userId auto-populated
    return dispatch(new UnlinkTelegramRequest());
  }
}
