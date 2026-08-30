package com.personalfinance.auth.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * User DTO shared via gRPC adapter.
 * Used by consuming services to represent user data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String displayName;
    private String email;
    private String telegramChatId;
    private String avatarUrl;
}
