package com.personalfinance.common.cache.eviction;

import lombok.Builder;

@Builder(toBuilder = true)
public record EntityChangedEvent(Object entity, ChangeType changeType) {
  public enum ChangeType {CREATED, UPDATED, DELETED}
}
