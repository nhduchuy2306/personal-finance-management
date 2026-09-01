package com.personalfinance.common.schedule.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Abstract base class for all scheduled Quartz jobs in the project.
 * Provides built-in logging (start/end/duration) and error handling.
 */
@Slf4j
public abstract class AbstractScheduledJob extends QuartzJobBean {

  /**
   * Implement job logic here.
   * Spring beans are available via constructor injection.
   *
   * @param context Quartz job execution context
   */
  protected abstract void executeJob(JobExecutionContext context);

  /**
   * Template method — wraps executeJob with logging and error handling.
   * Do NOT override this — override {@link #executeJob} instead.
   */
  @Override
  protected final void executeInternal(JobExecutionContext context) throws JobExecutionException {
    String jobName = context.getJobDetail().getKey().getName();
    long startTime = System.currentTimeMillis();

    log.info("[Quartz] Job started: {}", jobName);
    try {
      executeJob(context);
      long duration = System.currentTimeMillis() - startTime;
      log.info("[Quartz] Job completed: {} ({}ms)", jobName, duration);
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("[Quartz] Job failed: {} ({}ms) — {}", jobName, duration, e.getMessage(), e);
      throw new JobExecutionException(e);
    }
  }
}
