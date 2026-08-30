package com.personalfinance.common.cache.key;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Centralized cache key builder.
 * All Redis keys in the system are built through these static methods.
 * NEVER hardcode key strings in handlers — always use this builder.
 */
public final class CacheKeyBuilder {

    private CacheKeyBuilder() {
    }

    // ── User ──
    public static String userProfile(UUID userId) {
        return "user:" + userId;
    }

    // ── Telegram OTP ──
    public static String telegramOtp(String otpCode) {
        return "telegram:otp:" + otpCode;
    }

    // ── Budget ──
    public static String activeBudget(UUID userId) {
        return "budget:active:" + userId;
    }

    // ── Spending counters ──
    public static String dailySpending(UUID userId, UUID categoryId, LocalDate date) {
        return "spending:daily:" + userId + ":" + categoryId + ":" + date;
    }

    public static String monthlySpending(UUID userId, UUID categoryId, String yearMonth) {
        return "spending:monthly:" + userId + ":" + categoryId + ":" + yearMonth;
    }

    // ── Alert dedup ──
    public static String alertSent(UUID userId, UUID categoryId, String alertType, LocalDate date) {
        return "alert:sent:" + userId + ":" + categoryId + ":" + alertType + ":" + date;
    }

    // ── Notification dedup ──
    public static String notifSent(UUID userId, String type, String entityId, LocalDate date) {
        return "notif:sent:" + userId + ":" + type + ":" + entityId + ":" + date;
    }

    // ── Group balances ──
    public static String groupBalance(UUID groupId) {
        return "group:balance:" + groupId;
    }

    // ── Savings progress ──
    public static String savingProgress(UUID goalId) {
        return "saving:progress:" + goalId;
    }

    // ── Upcoming bills ──
    public static String upcomingBills(UUID userId) {
        return "bills:upcoming:" + userId;
    }

    // ── Receipt OCR status ──
    public static String receiptStatus(UUID receiptId) {
        return "receipt:status:" + receiptId;
    }
}
