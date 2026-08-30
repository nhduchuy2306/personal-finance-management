package com.personalfinance.auth.grpc.server;

import com.personalfinance.auth.adapter.proto.AuthServiceGrpc;
import com.personalfinance.auth.adapter.proto.GetUserRequest;
import com.personalfinance.auth.adapter.proto.GetUserTelegramRequest;
import com.personalfinance.auth.adapter.proto.GetUsersByIdsRequest;
import com.personalfinance.auth.adapter.proto.GetUsersByIdsResponse;
import com.personalfinance.auth.adapter.proto.TelegramChatIdResponse;
import com.personalfinance.auth.adapter.proto.UserResponse;
import com.personalfinance.common.base.exception.BusinessException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

/**
 * Auth gRPC server — delegates to individual gRPC handlers.
 * Lives in auth-service (not auth-adapter) because handlers need DB access.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuthGrpcServer extends AuthServiceGrpc.AuthServiceImplBase {

  private final GetUserByIdGrpcHandler getUserByIdHandler;
  private final GetUsersByIdsGrpcHandler getUsersByIdsHandler;
  private final GetUserTelegramChatIdGrpcHandler getUserTelegramChatIdHandler;

  @Override
  public void getUserById(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
    try {
      UserResponse response = getUserByIdHandler.execute(request);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (BusinessException e) {
      log.warn("gRPC getUserById failed: {}", e.getMessage());
      responseObserver.onError(
        Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("gRPC getUserById unexpected error: {}", e.getMessage(), e);
      responseObserver.onError(
        Status.INTERNAL.withDescription("Internal error").asRuntimeException());
    }
  }

  @Override
  public void getUsersByIds(GetUsersByIdsRequest request, StreamObserver<GetUsersByIdsResponse> responseObserver) {
    try {
      GetUsersByIdsResponse response = getUsersByIdsHandler.execute(request);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("gRPC getUsersByIds failed: {}", e.getMessage(), e);
      responseObserver.onError(
        Status.INTERNAL.withDescription("Internal error").asRuntimeException());
    }
  }

  @Override
  public void getUserTelegramChatId(GetUserTelegramRequest request, StreamObserver<TelegramChatIdResponse> responseObserver) {
    try {
      TelegramChatIdResponse response = getUserTelegramChatIdHandler.execute(request);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (BusinessException e) {
      log.warn("gRPC getUserTelegramChatId failed: {}", e.getMessage());
      responseObserver.onError(
        Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log.error("gRPC getUserTelegramChatId unexpected error: {}", e.getMessage(), e);
      responseObserver.onError(
        Status.INTERNAL.withDescription("Internal error").asRuntimeException());
    }
  }
}
