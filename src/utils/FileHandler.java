package utils;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton file handler for all .txt file operations.
 * Ensures only one instance writes to files at a time, preventing data loss.
 *
 * Data format: pipe-delimited (:::) .txt files stored in the "data/" directory.
 */
public class FileHandler {

    // ───── Singleton Instance ─────
    private static FileHandler instance;

    // ───── File Paths ─────
    private static final String DATA_DIR = "data";
    public static final String USERS_FILE = DATA_DIR + File.separator + "users.txt";
    public static final String APPOINTMENTS_FILE = DATA_DIR + File.separator + "appointments.txt";
    public static final String PAYMENTS_FILE = DATA_DIR + File.separator + "payments.txt";
    public static final String SERVICE_PRICES_FILE = DATA_DIR + File.separator + "service_prices.txt";
    public static final String NOTIFICATIONS_FILE = DATA_DIR + File.separator + "notifications.txt";

    public static final String DELIMITER = ":::";
    public static final String SEPARATOR = ":::";

    // ───── Private Constructor (Singleton) ─────
    private FileHandler() {
        // Create data directory if it doesn't exist
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Create files if they don't exist
        
        // Format: userId:::username:::hashedPassword:::name:::email:::phone:::role:::specialization
        createFileIfNotExists(USERS_FILE);
        
        // Format: appointmentId:::customerId:::technicianId:::serviceType:::status:::dateTime:::endDateTime:::comments:::feedback:::serviceReview
        createFileIfNotExists(APPOINTMENTS_FILE);
        
        // Format: paymentId:::appointmentId:::amount:::paymentMethod:::paymentStatus:::dateTime
        createFileIfNotExists(PAYMENTS_FILE);
        
        // Format: serviceType:::price
        createFileIfNotExists(SERVICE_PRICES_FILE);
        
        // Format: notificationId:::targetUserId:::dateTime:::message:::isRead
        createFileIfNotExists(NOTIFICATIONS_FILE);
    }

    /**
     * Returns the single instance of FileHandler.
     *
     * @return the FileHandler singleton instance
     */
    public static synchronized FileHandler getInstance() {
        if (instance == null) {
            instance = new FileHandler();
        }
        return instance;
    }

    // ═══════════════════════════════════════════
    //  CORE FILE OPERATIONS
    // ═══════════════════════════════════════════

    /**
     * Reads all lines from the specified file.
     *
     * @param filePath the path to the file
     * @return a list of all lines in the file
     */
    public List<String> readAllLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath + " - " + e.getMessage());
        }
        return lines;
    }

    /**
     * Writes all lines to the specified file, overwriting existing content.
     *
     * @param filePath the path to the file
     * @param lines    the lines to write
     */
    public void writeAllLines(String filePath, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filePath + " - " + e.getMessage());
        }
    }

    /**
     * Appends a single line to the specified file.
     *
     * @param filePath the path to the file
     * @param line     the line to append
     */
    public void appendLine(String filePath, String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error appending to file: " + filePath + " - " + e.getMessage());
        }
    }

    /**
     * Deletes a line from the file that starts with the given key.
     *
     * @param filePath the path to the file
     * @param key      the key to match at the beginning of the line (e.g. userId)
     */
    public void deleteLine(String filePath, String key) {
        List<String> lines = readAllLines(filePath);
        List<String> updatedLines = lines.stream()
                .filter(line -> !line.startsWith(key + SEPARATOR))
                .collect(Collectors.toList());
        writeAllLines(filePath, updatedLines);
    }

    /**
     * Updates a line in the file. Finds the line starting with the given key
     * and replaces it with the new line.
     *
     * @param filePath the path to the file
     * @param key      the key to match at the beginning of the line
     * @param newLine  the replacement line
     */
    public void updateLine(String filePath, String key, String newLine) {
        List<String> lines = readAllLines(filePath);
        List<String> updatedLines = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith(key + SEPARATOR)) {
                updatedLines.add(newLine);
            } else {
                updatedLines.add(line);
            }
        }
        writeAllLines(filePath, updatedLines);
    }

    /**
     * Searches for a line starting with the given key.
     *
     * @param filePath the path to the file
     * @param key      the key to search for
     * @return the matching line, or null if not found
     */
    public String findLineByKey(String filePath, String key) {
        List<String> lines = readAllLines(filePath);
        return lines.stream()
                .filter(line -> line.startsWith(key + SEPARATOR))
                .findFirst()
                .orElse(null);
    }

    /**
     * Generates the next unique ID by reading the file and incrementing the max ID.
     *
     * @param filePath the path to the file
     * @param prefix   the prefix for the ID (e.g. "CUS", "APT", "PAY")
     * @return a new unique ID string
     */
    public String generateNextId(String filePath, String prefix) {
        List<String> lines = readAllLines(filePath);
        int maxNum = 0;
        for (String line : lines) {
            String[] parts = line.split(DELIMITER);
            if (parts.length > 0 && parts[0].startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(parts[0].substring(prefix.length()));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException e) {
                    // Skip lines with invalid IDs
                }
            }
        }
        return prefix + String.format("%04d", maxNum + 1);
    }

    // ═══════════════════════════════════════════
    //  HELPER METHODS
    // ═══════════════════════════════════════════

    /**
     * Creates a file if it does not already exist.
     *
     * @param filePath the path to the file
     */
    private void createFileIfNotExists(String filePath) {
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Error creating file: " + filePath + " - " + e.getMessage());
        }
    }
}
