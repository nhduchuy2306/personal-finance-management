package com.personalfinance.common.base.request;

import java.util.UUID;

/**
 * Interface for requests that require the authenticated user's ID.
 */
public interface UserAwareRequest extends BaseRequest {

  UUID getUserId();

  void setUserId(UUID userId);
}
