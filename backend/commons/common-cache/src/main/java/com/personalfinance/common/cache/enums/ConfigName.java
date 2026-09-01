package com.personalfinance.common.cache.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Single source of truth for all dynamic system configuration keys.
 * Each entry has a defaultValue used as fallback when not present in DB.
 *
 * <p>Usage:
 * <pre>
 *   String value = systemConfigService.getValue(ConfigName.CACHE_TTL_DEFAULT);
 *   Duration ttl = systemConfigService.getAsDuration(ConfigName.CACHE_TTL_USER_PROFILE);
 *   int threshold = systemConfigService.getAsInt(ConfigName.ALERT_THRESHOLD_PERCENT);
 * </pre>
 */
@Getter
@RequiredArgsConstructor
public enum ConfigName {

  // ── Cache TTL configs (value = seconds) ──
  CACHE_TTL_DEFAULT("3600", "Default cache TTL in seconds"),
  CACHE_TTL_USER_PROFILE("1800", "User profile cache TTL in seconds"),
  CACHE_TTL_ACTIVE_BUDGET("3600", "Active budget cache TTL in seconds"),
  CACHE_TTL_GROUP_BALANCE("3600", "Group balance cache TTL in seconds"),
  CACHE_TTL_SAVING_PROGRESS("3600", "Saving progress cache TTL in seconds"),
  CACHE_TTL_UPCOMING_BILLS("3600", "Upcoming bills cache TTL in seconds"),

  // ── Alert configs ──
  ALERT_THRESHOLD_PERCENT("80", "Spending alert threshold percentage"),

  // ── Notification configs ──
  MAX_DAILY_NOTIFICATIONS("10", "Maximum notifications per user per day"),
  NOTIFICATION_COOLDOWN_MINUTES("30", "Minutes between similar notifications"),

  // ── Budget configs ──
  BUDGET_AUTO_FILL_ENABLED("true", "Enable budget auto-fill from previous month"),

  // ── OCR configs ──
  OCR_MAX_FILE_SIZE_MB("10", "Maximum receipt file size in MB"),
  ;

  private final String defaultValue;
  private final String description;
}
