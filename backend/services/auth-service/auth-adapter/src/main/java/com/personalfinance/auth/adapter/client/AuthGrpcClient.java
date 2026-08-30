package com.personalfinance.auth.adapter.client;

import com.personalfinance.auth.adapter.dto.UserDto;
import com.personalfinance.auth.adapter.proto.AuthServiceGrpc;
import com.personalfinance.auth.adapter.proto.GetUserRequest;
import com.personalfinance.auth.adapter.proto.GetUserTelegramRequest;
import com.personalfinance.auth.adapter.proto.GetUsersByIdsRequest;
import com.personalfinance.auth.adapter.proto.GetUsersByIdsResponse;
import com.personalfinance.auth.adapter.proto.TelegramChatIdResponse;
import com.personalfinance.auth.adapter.proto.UserResponse;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Auth gRPC client — used by other services to query user info.
 * Lives in auth-adapter so other services can depend on it as a library.
 * <p>
 * Uses Spring Boot 4.1 native gRPC support via GrpcChannelFactory.
 */
@Slf4j
@Component
public class AuthGrpcClient {

  private final AuthServiceGrpc.AuthServiceBlockingStub stub;

  public AuthGrpcClient(GrpcChannelFactory channels) {
    Channel channel = channels.createChannel("auth-service");
    this.stub = AuthServiceGrpc.newBlockingStub(channel);
  }

  /**
   * Get single user by ID.
   */
  public UserDto getUserById(UUID userId) {
    GetUserRequest request = GetUserRequest.newBuilder()
      .setUserId(userId.toString())
      .build();
    UserResponse response = stub.getUserById(request);
    return mapToDomain(response);
  }

  /**
   * Get multiple users by IDs (batch).
   */
  public List<UserDto> getUsersByIds(List<UUID> userIds) {
    GetUsersByIdsRequest request = GetUsersByIdsRequest.newBuilder()
      .addAllUserIds(userIds.stream().map(UUID::toString).toList())
      .build();
    GetUsersByIdsResponse response = stub.getUsersByIds(request);
    return response.getUsersList().stream()
      .map(this::mapToDomain)
      .toList();
  }

  /**
   * Get user's Telegram chat ID for notification.
   */
  public String getUserTelegramChatId(UUID userId) {
    GetUserTelegramRequest request = GetUserTelegramRequest.newBuilder()
      .setUserId(userId.toString())
      .build();
    TelegramChatIdResponse response = stub.getUserTelegramChatId(request);
    return response.getHasTelegram() ? response.getChatId() : null;
  }

  private UserDto mapToDomain(UserResponse response) {
    return UserDto.builder()
      .id(UUID.fromString(response.getUserId()))
      .displayName(response.getDisplayName())
      .email(response.getEmail())
      .telegramChatId(response.getTelegramChatId().isEmpty() ? null : response.getTelegramChatId())
      .avatarUrl(response.getAvatarUrl().isEmpty() ? null : response.getAvatarUrl())
      .build();
  }
}
