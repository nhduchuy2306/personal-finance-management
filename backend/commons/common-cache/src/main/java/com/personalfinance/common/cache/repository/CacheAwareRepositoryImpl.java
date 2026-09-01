package com.personalfinance.common.cache.repository;

import com.personalfinance.common.cache.eviction.EntityChangedEvent;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class CacheAwareRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID>
  implements CacheAwareRepository<T, ID> {

  @Autowired
  private ApplicationEventPublisher eventPublisher;
  private final JpaEntityInformation<T, ID> entityInformation;

  public CacheAwareRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager em) {
    super(entityInformation, em);
    this.entityInformation = entityInformation;
  }

  @Override
  @NullMarked
  public <S extends T> S save(S entity) {
    boolean isNew = entityInformation.isNew(entity);
    S saved = super.save(entity);

    // Publish AFTER save succeeds, BEFORE transaction commits
    // @TransactionalEventListener(AFTER_COMMIT) ensures cache eviction
    // only happens if transaction commits successfully
    eventPublisher.publishEvent(new EntityChangedEvent(
      saved,
      isNew ? EntityChangedEvent.ChangeType.CREATED : EntityChangedEvent.ChangeType.UPDATED
    ));

    return saved;
  }

  @Override
  public void delete(@NonNull T entity) {
    super.delete(entity);
    eventPublisher.publishEvent(new EntityChangedEvent(entity, EntityChangedEvent.ChangeType.DELETED));
  }

  @Override
  public void deleteById(@NonNull ID id) {
    findById(id).ifPresent(this::delete);
  }
}
