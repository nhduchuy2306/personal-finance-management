package com.personalfinance.common.base.response;

/**
 * Marker interface for all response DTOs.
 * All responses returned by Handlers MUST implement this interface.
 * <p>
 * Use specialized implementations for common patterns:
 * <ul>
 *   <li>{@link VoidResponse} — for handlers that return nothing</li>
 *   <li>{@link PageableResponse} — for paginated query results</li>
 * </ul>
 */
public interface BaseResponse {
}
