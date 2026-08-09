package com.mtech.adms.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Minimal logging utility. Wraps console output with a consistent
 * timestamp + level format, so we're not scattering raw
 * System.out.println() calls throughout the codebase.
 *
 * This is intentionally simple for now. If the project later needs
 * log file output, log rotation, or filtering by level, this class
 * can be swapped for a real framework (e.g. SLF4J + Logback) without
 * changing any calling code, since callers only depend on this
 * class's static methods.
 */
public final class AppLogger {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {
    }

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void error(String message, Throwable cause) {
        log("ERROR", message + " | Cause: " + cause.getMessage());
    }

    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.out.println("[" + timestamp + "] [" + level + "] " + message);
    }
}