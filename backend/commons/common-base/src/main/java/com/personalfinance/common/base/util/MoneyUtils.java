package com.personalfinance.common.base.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilities for VNĐ currency formatting.
 * All monetary values are stored as BIGINT (long) in VNĐ.
 */
public final class MoneyUtils {

    private static final Locale VN_LOCALE = Locale.of("vi", "VN");
    private static final NumberFormat VND_FORMAT = NumberFormat.getCurrencyInstance(VN_LOCALE);

    private MoneyUtils() {
    }

    /**
     * Format amount to VNĐ string (e.g., "85.000 ₫").
     */
    public static String format(long amount) {
        return VND_FORMAT.format(amount);
    }

    /**
     * Format amount with sign (e.g., "+85.000 ₫" or "-50.000 ₫").
     */
    public static String formatWithSign(long amount) {
        String formatted = VND_FORMAT.format(Math.abs(amount));
        return (amount >= 0 ? "+" : "-") + formatted;
    }

    /**
     * Calculate percentage: (current / total) * 100.
     * Returns 0 if total is 0.
     */
    public static double percentage(long current, long total) {
        if (total == 0) return 0;
        return (double) current / total * 100;
    }

    /**
     * Check if amount exceeds threshold percentage.
     */
    public static boolean exceedsThreshold(long current, long total, int thresholdPercent) {
        return percentage(current, total) >= thresholdPercent;
    }
}
