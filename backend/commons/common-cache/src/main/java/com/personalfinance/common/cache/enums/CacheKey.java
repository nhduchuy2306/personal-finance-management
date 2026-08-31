package com.personalfinance.common.cache.enums;

import com.personalfinance.common.cache.registry.CacheKeyDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Single source of truth for all Redis cache key patterns.
 * All Redis keys in the system are built through this enum.
 * NEVER hardcode key strings in handlers — always use CacheKey.XXX.buildKey(...).
 *
 * <p>Usage:
 * <pre>
 *   // Build a concrete key for cache read/write:
 *   String key = CacheKey.USER_PROFILE.buildKey(userId);
 *
 *   // Build an eviction key (unknown args replaced with *):
 *   String evictKey = CacheKey.DAILY_SPENDING.buildEvictKey(userId);
 *   // → "spending:daily:{userId}:*:*"
 * </pre>
 */
@Getter
@RequiredArgsConstructor
public enum CacheKey implements CacheKeyDefinition {

  // ── User ──
  USER_PROFILE("user:%s", EvictStrategy.EXACT),

  // ── Telegram OTP ──
  TELEGRAM_OTP("telegram:otp:%s", EvictStrategy.EXACT),

  // ── Budget ──
  ACTIVE_BUDGET("budget:active:%s", EvictStrategy.EXACT),

  // ── Spending counters ──
  DAILY_SPENDING("spending:daily:%s:%s:%s", EvictStrategy.EXACT),
  MONTHLY_SPENDING("spending:monthly:%s:%s:%s", EvictStrategy.EXACT),

  // ── Alert dedup ──
  ALERT_SENT("alert:sent:%s:%s:%s:%s", EvictStrategy.EXACT),

  // ── Notification dedup ──
  NOTIF_SENT("notif:sent:%s:%s:%s:%s", EvictStrategy.EXACT),

  // ── Group balances ──
  GROUP_BALANCE("group:balance:%s", EvictStrategy.EXACT),

  // ── Savings progress ──
  SAVING_PROGRESS("saving:progress:%s", EvictStrategy.EXACT),

  // ── Upcoming bills ──
  UPCOMING_BILLS("bills:upcoming:%s", EvictStrategy.EXACT),

  // ── Receipt OCR status ──
  RECEIPT_STATUS("receipt:status:%s", EvictStrategy.EXACT),
  ;

  private final String pattern;
  private final EvictStrategy evictStrategy;
}
