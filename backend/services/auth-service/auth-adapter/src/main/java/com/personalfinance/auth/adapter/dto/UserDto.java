package com.personalfinance.auth.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * User DTO shared via gRPC adapter.
 * Used by consuming services to represent user data.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

  private UUID id;
  private String displayName;
  private String email;
  private String telegramChatId;
  private String avatarUrl;
}
