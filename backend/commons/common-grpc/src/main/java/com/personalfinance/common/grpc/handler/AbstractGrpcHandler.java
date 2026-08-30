package com.personalfinance.common.grpc.handler;

/**
 * Base handler for gRPC service methods.
 * Enforces bidirectional mapping between gRPC (protobuf) and domain objects.
 *
 * @param <G_REQ> gRPC request (protobuf generated)
 * @param <G_RES> gRPC response (protobuf generated)
 * @param <D_REQ> Domain request (internal DTO)
 * @param <D_RES> Domain response (internal DTO)
 */
public abstract class AbstractGrpcHandler<G_REQ, G_RES, D_REQ, D_RES> {

    /**
     * Convert gRPC request → domain request.
     * MUST be overridden — forces developer to explicitly define the mapping.
     */
    protected abstract D_REQ mapFromGrpc(G_REQ grpcRequest);

    /**
     * Convert domain response → gRPC response.
     * MUST be overridden — forces developer to explicitly define the mapping.
     */
    protected abstract G_RES mapToGrpc(D_RES domainResponse);

    /**
     * Core business logic using domain objects.
     */
    protected abstract D_RES handle(D_REQ domainRequest);

    /**
     * Template method — do not override.
     * Handles the full flow: map in → process → map out.
     */
    public final G_RES execute(G_REQ grpcRequest) {
        D_REQ domainRequest = mapFromGrpc(grpcRequest);
        D_RES domainResponse = handle(domainRequest);
        return mapToGrpc(domainResponse);
    }
}
