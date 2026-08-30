package com.personalfinance.common.event.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base event class for all Kafka events.
 * All event DTOs extend this.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {

  /**
   * UUID, generated automatically by EventPublisher
   */
  private String eventId;

  /**
   * Event type (e.g. "transaction.created")
   */
  private String eventType;

  /**
   * Event timestamp (VN timezone)
   */
  private LocalDateTime timestamp;

  /**
   * Service name that produced this event
   */
  private String source;

  /**
   * Who triggered this event
   */
  private UUID userId;
}
