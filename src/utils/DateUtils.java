package utils;

import java.time.format.DateTimeFormatter;

/**
 * Utility class for common date and time operations.
 * Ensures an application-wide standard format.
 */
public class DateUtils {

    /**
     * Standard date-time formatter used across Models, Services, and UI.
     * Format: "yyyy-MM-dd HH:mm" (e.g., "2026-03-30 14:30")
     */
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateUtils() {
        // Private constructor to prevent instantiation of utility class
    }
}
