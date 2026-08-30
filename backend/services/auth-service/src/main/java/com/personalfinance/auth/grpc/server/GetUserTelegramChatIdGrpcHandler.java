package com.personalfinance.auth.grpc.server;

import com.personalfinance.auth.adapter.dto.UserDto;
import com.personalfinance.auth.adapter.proto.GetUserTelegramRequest;
import com.personalfinance.auth.adapter.proto.TelegramChatIdResponse;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.grpc.handler.AbstractGrpcHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * gRPC handler — GetUserTelegramChatId.
 * Returns the user's Telegram chat ID for notification service.
 */
@Component
@RequiredArgsConstructor
public class GetUserTelegramChatIdGrpcHandler
  extends AbstractGrpcHandler<GetUserTelegramRequest, TelegramChatIdResponse, UUID, UserDto> {

  private final UserRepository userRepository;

  @Override
  protected UUID mapFromGrpc(GetUserTelegramRequest grpcRequest) {
    return UUID.fromString(grpcRequest.getUserId());
  }

  @Override
  protected TelegramChatIdResponse mapToGrpc(UserDto user) {
    boolean hasTelegram = user.getTelegramChatId() != null && !user.getTelegramChatId().isEmpty();
    return TelegramChatIdResponse.newBuilder()
      .setUserId(user.getId().toString())
      .setChatId(hasTelegram ? user.getTelegramChatId() : "")
      .setHasTelegram(hasTelegram)
      .build();
  }

  @Override
  protected UserDto handle(UUID userId) {
    return userRepository.findById(userId)
      .map(user -> UserDto.builder()
        .id(user.getId())
        .displayName(user.getDisplayName())
        .email(user.getEmail())
        .telegramChatId(user.getTelegramChatId())
        .avatarUrl(user.getAvatarUrl())
        .build())
      .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }
}
