package com.personalfinance.common.cache.registry;

import com.personalfinance.common.cache.eviction.EntityChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CacheEvictionListener {
  private final CacheEvictionRegistry evictionRegistry;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onEntityChange(EntityChangedEvent event) {
    evictionRegistry.evictFor(event.entity());
  }
}
