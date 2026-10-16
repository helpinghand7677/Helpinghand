package com.helpinghand.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Tiny in-memory brute-force guard for login endpoints: after MAX_FAILURES wrong
 * attempts for the same key (client IP + phone), further attempts are blocked for
 * LOCK_MS. A successful login clears the counter. State is per-JVM (fine for a
 * single Tomcat; use a shared store if you ever run several instances).
 */
public class LoginThrottle {

    private static final int MAX_FAILURES = 5;
    private static final long LOCK_MS = 10 * 60 * 1000L;

    private static class Entry { int failures; long lockedUntil; }

    private static final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

    public static boolean isBlocked(String key) {
        Entry e = entries.get(key);
        if (e == null) return false;
        synchronized (e) {
            if (e.lockedUntil > System.currentTimeMillis()) return true;
            if (e.lockedUntil != 0) { e.failures = 0; e.lockedUntil = 0; } // lock expired
            return false;
        }
    }

    public static void recordFailure(String key) {
        Entry e = entries.computeIfAbsent(key, k -> new Entry());
        synchronized (e) {
            if (++e.failures >= MAX_FAILURES) e.lockedUntil = System.currentTimeMillis() + LOCK_MS;
        }
    }

    public static void recordSuccess(String key) {
        entries.remove(key);
    }
}
