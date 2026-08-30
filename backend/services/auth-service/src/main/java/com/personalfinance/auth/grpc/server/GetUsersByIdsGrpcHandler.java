package com.personalfinance.auth.grpc.server;

import com.personalfinance.auth.adapter.dto.UserDto;
import com.personalfinance.auth.adapter.proto.GetUsersByIdsRequest;
import com.personalfinance.auth.adapter.proto.GetUsersByIdsResponse;
import com.personalfinance.auth.adapter.proto.UserResponse;
import com.personalfinance.auth.repository.UserRepository;
import com.personalfinance.common.grpc.handler.AbstractGrpcHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * gRPC handler — GetUsersByIds (batch).
 * Maps: GetUsersByIdsRequest → List<UUID> → List<UserDto> → GetUsersByIdsResponse.
 */
@Component
@RequiredArgsConstructor
public class GetUsersByIdsGrpcHandler
  extends AbstractGrpcHandler<GetUsersByIdsRequest, GetUsersByIdsResponse, List<UUID>, List<UserDto>> {

  private final UserRepository userRepository;

  @Override
  protected List<UUID> mapFromGrpc(GetUsersByIdsRequest grpcRequest) {
    return grpcRequest.getUserIdsList().stream()
      .map(UUID::fromString)
      .toList();
  }

  @Override
  protected GetUsersByIdsResponse mapToGrpc(List<UserDto> users) {
    List<UserResponse> responses = users.stream()
      .map(user -> UserResponse.newBuilder()
        .setUserId(user.getId().toString())
        .setDisplayName(user.getDisplayName())
        .setEmail(user.getEmail())
        .setTelegramChatId(user.getTelegramChatId() != null ? user.getTelegramChatId() : "")
        .setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : "")
        .build())
      .toList();

    return GetUsersByIdsResponse.newBuilder()
      .addAllUsers(responses)
      .build();
  }

  @Override
  protected List<UserDto> handle(List<UUID> userIds) {
    return userRepository.findByIdIn(userIds).stream()
      .map(user -> UserDto.builder()
        .id(user.getId())
        .displayName(user.getDisplayName())
        .email(user.getEmail())
        .telegramChatId(user.getTelegramChatId())
        .avatarUrl(user.getAvatarUrl())
        .build())
      .toList();
  }
}
