package com.personalfinance.auth.features.systemconfig.job;

import com.personalfinance.auth.features.systemconfig.service.SystemConfigCacheWarmer;
import com.personalfinance.common.schedule.job.AbstractScheduledJob;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * Quartz job for system config cache warm-up.
 * Runs daily at 3:00 AM (configured in SystemConfigScheduleConfig).
 * Delegates to SystemConfigCacheWarmer.warmAll() to reload all configs from DB → Redis.
 */
@Component
@RequiredArgsConstructor
public class SystemConfigWarmUpJob extends AbstractScheduledJob {

  private final SystemConfigCacheWarmer cacheWarmer;

  @Override
  protected void executeJob(JobExecutionContext context) {
    int count = cacheWarmer.warmAll();
    context.setResult("Warmed " + count + " config entries");
  }
}
