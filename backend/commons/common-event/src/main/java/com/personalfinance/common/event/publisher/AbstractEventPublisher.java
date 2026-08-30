package com.personalfinance.common.event.publisher;

import com.personalfinance.common.event.model.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Abstract event publisher with template method pattern.
 * NEVER publish domain models directly to Kafka — always map to event DTOs.
 *
 * @param <S> Source object (domain model or response DTO)
 * @param <E> Event object (what gets published to Kafka)
 */
@Slf4j
public abstract class AbstractEventPublisher<S, E extends BaseEvent> {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    protected AbstractEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * MUST override: which Kafka topic to publish to.
     */
    protected abstract String getTopic();

    /**
     * MUST override: convert source object → event DTO.
     * Select ONLY the fields needed for the event.
     */
    protected abstract E mapToEvent(S source);

    /**
     * MUST override: partition key for ordering guarantee.
     * Typically userId or entityId.
     */
    protected abstract String getPartitionKey(E event);

    /**
     * Optional: enrich event before publishing.
     * Default fills eventId + timestamp.
     */
    protected E enrichEvent(E event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        }
        return event;
    }

    /**
     * Template method — maps source → event → enriches → publishes.
     */
    public final void publish(S source) {
        try {
            E event = mapToEvent(source);
            event = enrichEvent(event);
            String key = getPartitionKey(event);

            kafkaTemplate.send(getTopic(), key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event to {}: {}", getTopic(), ex.getMessage(), ex);
                        } else {
                            log.debug("Published event to {} [partition={}, offset={}]",
                                    getTopic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing to {}: {}", getTopic(), e.getMessage(), e);
        }
    }

    /**
     * Batch publish — for cases like OCR where multiple items are confirmed at once.
     */
    public final void publishAll(List<S> sources) {
        sources.forEach(this::publish);
    }
}
