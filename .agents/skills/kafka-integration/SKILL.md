---
name: kafka-integration
description: >-
  Use this skill when the user asks to add Kafka messaging (producer/consumer)
  to a service, create new Kafka topics, or implement event-driven communication
  between services.
---

# Kafka Integration

## Topic Naming Convention

Topics are defined in `common-service/src/main/java/com/gyp/common/constants/TopicConstants.java`.

Pattern: `{domain}.{action}.event` (e.g., `event.command.create`, `generate.ticket.pdf.event`)

## 1. Add Kafka Dependency

In `{service-name}.gradle`:
```groovy
implementation "org.springframework.kafka:spring-kafka:${kafkaVersion}"
```

## 2. Define Topic Constant

In `common-service/.../constants/TopicConstants.java`:
```java
public static final String YOUR_TOPIC = "your.domain.event";
```

## 3. Kafka Producer

```java
package com.gyp.{servicename}.messages.producers;

import com.gyp.common.constants.TopicConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class {Resource}Producer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(Object payload) {
        try {
            kafkaTemplate.send(TopicConstants.YOUR_TOPIC, payload);
            log.info("Sent message to topic: {}", TopicConstants.YOUR_TOPIC);
        } catch (Exception e) {
            log.error("Failed to send message to topic: {}", TopicConstants.YOUR_TOPIC, e);
        }
    }
}
```

## 4. Kafka Consumer

```java
package com.gyp.{servicename}.messages.consumers;

import com.gyp.common.constants.TopicConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class {Resource}Consumer {

    @KafkaListener(topics = TopicConstants.YOUR_TOPIC, groupId = "{service-name}-group")
    public void consume(String message) {
        try {
            log.info("Received message from topic: {}", TopicConstants.YOUR_TOPIC);
            // Process message
        } catch (Exception e) {
            // WARNING: No DLQ configured — failed messages are logged and lost
            log.error("Failed to process message from topic: {}", TopicConstants.YOUR_TOPIC, e);
        }
    }
}
```

## 5. Kafka Configuration (application.properties)

```properties
# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id={service-name}-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

## Existing Topics Reference

| Topic | Producer | Consumer | Payload |
|-------|----------|----------|---------|
| `event.command.create` | event-service | salechannel-service | EventEntity |
| `event.command.update` | event-service | salechannel-service | EventEntity |
| `event.command.delete` | event-service | salechannel-service | String (eventId) |
| `generate.ticket.pdf.event` | event-service | ticket-service | TicketGenerationModel |
| `send-email.event` | ticket-service | notification-service | EmailModel |
| `order.created.event` | order-service | event-service | OrderModel |
| `event.on.sale.event` | event-service | salechannel-service | EventOnSaleModel |

## Profile-Based Disabling

Use profile `no-kafka` to skip Kafka in dev:
```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
```

## ⚠️ Known Limitation

There is NO Dead Letter Queue (DLQ) configured. Failed message processing is logged but the message is lost. Consider adding DLQ for production.
