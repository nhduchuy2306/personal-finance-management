package com.personalfinance.auth.features.systemconfig.config;

import com.personalfinance.auth.features.systemconfig.job.SystemConfigWarmUpJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Quartz schedule configuration for auth-service jobs.
 * Cron expressions are externalized to config (schedule-dev.yml → app.schedule.*).
 */
@Configuration
public class SystemConfigScheduleConfig {
  private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

  @Value("${app.schedule.system-config-warm-up:0 0 3 * * ?}")
  private String configWarmUpCron;

  @Bean
  public JobDetail systemConfigWarmUpJobDetail() {
    return JobBuilder.newJob(SystemConfigWarmUpJob.class)
      .withIdentity("systemConfigWarmUpJob", "system-config")
      .withDescription("Daily cache warm-up for system config values")
      .storeDurably()
      .build();
  }

  @Bean
  public Trigger systemConfigWarmUpTrigger(JobDetail systemConfigWarmUpJobDetail) {
    return TriggerBuilder.newTrigger()
      .forJob(systemConfigWarmUpJobDetail)
      .withIdentity("systemConfigWarmUpTrigger", "system-config")
      .withSchedule(CronScheduleBuilder
        .cronSchedule(configWarmUpCron)
        .inTimeZone(TIMEZONE)
        .withMisfireHandlingInstructionFireAndProceed())
      .build();
  }
}
