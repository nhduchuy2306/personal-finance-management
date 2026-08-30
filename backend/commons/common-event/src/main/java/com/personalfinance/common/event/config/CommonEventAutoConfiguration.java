package com.personalfinance.common.event.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for common-event module.
 * Registers KafkaConsumerConfig, KafkaProducerConfig.
 */
@AutoConfiguration
@ComponentScan("com.personalfinance.common.event")
public class CommonEventAutoConfiguration {
}
