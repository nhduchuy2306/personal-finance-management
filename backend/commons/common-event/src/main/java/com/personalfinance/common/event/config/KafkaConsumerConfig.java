package com.personalfinance.common.event.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration — dual factory setup.
 * - autoCommitListenerFactory: for non-critical consumers (notification, logging)
 * - manualCommitListenerFactory: for critical consumers (transaction, budget, settlement)
 */
@Configuration
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  /**
   * AUTO commit — for non-critical consumers where at-least-once is OK
   * and message loss on crash is acceptable.
   */
  @Bean
  public ConsumerFactory<String, Object> autoCommitConsumerFactory() {
    Map<String, Object> props = commonProps();
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000);
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean("autoCommitListenerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, Object> autoCommitListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
      new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(autoCommitConsumerFactory());
    return factory;
  }

  /**
   * MANUAL commit — for critical consumers where message must not be lost.
   * Consumer must call acknowledgment.acknowledge() after successful processing.
   */
  @Bean
  public ConsumerFactory<String, Object> manualCommitConsumerFactory() {
    Map<String, Object> props = commonProps();
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean("manualCommitListenerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, Object> manualCommitListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
      new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(manualCommitConsumerFactory());
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    return factory;
  }

  private Map<String, Object> commonProps() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
    props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.personalfinance.*");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    return props;
  }
}
