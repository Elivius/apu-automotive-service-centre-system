package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogger {

    private AuditLogger() {
        // Private constructor to prevent instantiation of utility class
    }

    /**
     * Records an auditable user action to data/audit_log.txt.
     *
     * @param userId  the ID of the user performing the action (e.g., "CUS0001").
     *                Use "SYSTEM" or "UNKNOWN" when no specific user is responsible.
     * @param action  a short, uppercase identifier for the action
     *                (e.g., "LOGIN_SUCCESS", "BOOK_APPOINTMENT").
     * @param details a human-readable description with relevant context
     *                (e.g., "APT0003 | Normal | 2026-04-12 09:00").
     */
    public static void log(String userId, String action, String details) {
        String timestamp = LocalDateTime.now().format(DateUtils.FORMATTER);
        String entry = timestamp + FileHandler.SEPARATOR + nullSafe(userId) + FileHandler.SEPARATOR + nullSafe(action) + FileHandler.SEPARATOR + nullSafe(details);

        FileHandler.getInstance().appendLine(FileHandler.AUDIT_LOG_FILE, entry);
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }

    /**
     * Reads the audit log file and returns all entries in reverse chronological order
     * (newest first). Splits the pipe-delimited strings into arrays for UI consumption.
     */
    public static List<String[]> getAllLogEntriesReverse() {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.AUDIT_LOG_FILE);
        // ArrayList (list of log) to store Array (data of each log)
        List<String[]> entries = new ArrayList<>();

        // Parse newest-first for easier reading
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            String[] parts = line.split(FileHandler.DELIMITER, -1);
            if (parts.length >= 4) {
                entries.add(new String[]{parts[0], parts[1], parts[2], parts[3]});
            } else if (parts.length == 3) {
                entries.add(new String[]{parts[0], parts[1], parts[2], ""});
            }
        }
        return entries;
    }
}
