package com.personalfinance.common.base.request;

/**
 * Marker interface for all request DTOs.
 * All requests dispatched via HandlerRegistry MUST implement this interface.
 * <p>
 * Use specialized sub-interfaces for common patterns:
 * <ul>
 *   <li>{@link UserAwareRequest} — requests needing authenticated user's ID</li>
 *   <li>{@link PageableRequest} — requests with pagination parameters</li>
 * </ul>
 */
public interface BaseRequest {
}
