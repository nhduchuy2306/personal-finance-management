package com.personalfinance.common.base.constant;

import java.time.ZoneId;

/**
 * Application-wide constants.
 */
public final class AppConstants {

    private AppConstants() {
    }

    // Timezone
    public static final String VN_TIMEZONE = "Asia/Ho_Chi_Minh";
    public static final ZoneId VN_ZONE_ID = ZoneId.of(VN_TIMEZONE);

    // Currency
    public static final String CURRENCY = "VND";

    // Budget thresholds
    public static final int BUDGET_WARNING_THRESHOLD = 80;
    public static final int BUDGET_CRITICAL_THRESHOLD = 100;

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Telegram OTP
    public static final int OTP_LENGTH = 6;
    public static final int OTP_EXPIRY_MINUTES = 5;

    // Recurring bill reminder
    public static final int DEFAULT_REMINDER_DAYS_BEFORE = 3;

    // Bill estimate suggestion threshold (%)
    public static final int BILL_ESTIMATE_CHANGE_THRESHOLD = 20;

    // Cache TTLs (seconds)
    public static final long CACHE_TTL_SHORT = 300;         // 5 min
    public static final long CACHE_TTL_MEDIUM = 1800;       // 30 min
    public static final long CACHE_TTL_LONG = 3600;         // 1 hour
    public static final long CACHE_TTL_END_OF_DAY = 86400;  // 24 hours (approx)

    // Notification retry
    public static final int NOTIFICATION_MAX_RETRIES = 2;
}
