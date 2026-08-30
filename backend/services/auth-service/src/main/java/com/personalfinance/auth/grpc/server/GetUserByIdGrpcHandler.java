package com.personalfinance.auth.grpc.server;

import com.personalfinance.auth.adapter.dto.UserDto;
import com.personalfinance.auth.adapter.proto.GetUserRequest;
import com.personalfinance.auth.adapter.proto.UserResponse;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.base.exception.BusinessException;
import com.personalfinance.common.base.exception.ErrorCode;
import com.personalfinance.common.grpc.handler.AbstractGrpcHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * gRPC handler — GetUserById.
 * Maps: GetUserRequest → UUID → UserDto → UserResponse.
 */
@Component
@RequiredArgsConstructor
public class GetUserByIdGrpcHandler
  extends AbstractGrpcHandler<GetUserRequest, UserResponse, UUID, UserDto> {

  private final UserRepository userRepository;

  @Override
  protected UUID mapFromGrpc(GetUserRequest grpcRequest) {
    return UUID.fromString(grpcRequest.getUserId());
  }

  @Override
  protected UserResponse mapToGrpc(UserDto user) {
    return UserResponse.newBuilder()
      .setUserId(user.getId().toString())
      .setDisplayName(user.getDisplayName())
      .setEmail(user.getEmail())
      .setTelegramChatId(user.getTelegramChatId() != null ? user.getTelegramChatId() : "")
      .setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : "")
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
