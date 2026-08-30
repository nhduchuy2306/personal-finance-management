package com.personalfinance.common.security.context;

import java.util.UUID;

/**
 * ThreadLocal-based userId propagation.
 * Set by JwtAuthenticationFilter, consumed by handlers.
 */
public class UserContext {

    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();

    public static void set(UUID userId) {
        currentUserId.set(userId);
    }

    public static UUID getCurrentUserId() {
        return currentUserId.get();
    }

    public static void clear() {
        currentUserId.remove();
    }
}
