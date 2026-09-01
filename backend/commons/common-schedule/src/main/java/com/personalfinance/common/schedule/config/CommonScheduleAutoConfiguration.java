package com.personalfinance.common.schedule.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Autoconfiguration for common-schedule module.
 * Registers Quartz base classes and configuration.
 *
 * <p>Quartz Scheduler is auto-configured by Spring Boot via spring-boot-starter-quartz.
 * This module provides:
 * <ul>
 *   <li>{@code AbstractScheduledJob} — base class for all Quartz jobs with built-in logging and error handling</li>
 *   <li>Shared Quartz properties via config-service's {@code schedule.yml}</li>
 * </ul>
 */
@AutoConfiguration
@ComponentScan("com.personalfinance.common.schedule")
public class CommonScheduleAutoConfiguration {
}
