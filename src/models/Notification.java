package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Notification entity class.
 * Represents a system notification sent to a specific user.
 *
 * Supports targeted notifications (to a specific user) and
 * broadcast notifications (to all users of a role, or "ALL").
 */
public class Notification {

    // ───── Date Format ─────
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ───── Private Attributes (Encapsulation) ─────
    private String notificationId;
    private String targetUserId;    // e.g., "CUS0001", "ALL", "CounterStaff"
    private String message;
    private LocalDateTime dateTime;
    private boolean isRead;

    // ───── Constructors ─────
    public Notification() {
        this.isRead = false;
    }

    public Notification(String notificationId, String targetUserId, String message,
                        LocalDateTime dateTime, boolean isRead) {
        this.notificationId = notificationId;
        this.targetUserId = targetUserId;
        this.message = message;
        this.dateTime = dateTime;
        this.isRead = isRead;
    }

    // ═══════════════════════════════════════════
    //  GETTERS AND SETTERS (Encapsulation)
    // ═══════════════════════════════════════════

    public String getNotificationId() {
        return this.notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getTargetUserId() {
        return this.targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isRead() {
        return this.isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    // ═══════════════════════════════════════════
    //  FILE SERIALIZATION
    // ═══════════════════════════════════════════

    /**
     * Converts the notification to a delimited string for .txt file storage.
     * Format: notificationId:::targetUserId:::dateTime:::message:::isRead
     */
    public String toFileString() {
        return String.join(utils.FileHandler.SEPARATOR,
                safe(this.notificationId),
                safe(this.targetUserId),
                this.dateTime != null ? this.dateTime.format(FORMATTER) : "",
                safe(this.message),
                String.valueOf(this.isRead)); // convert boolean to "true" or "false"
    }

    /**
     * Parses a Notification object from a delimited file line.
     */
    public static Notification fromFileString(String line) {
        String[] parts = line.split(utils.FileHandler.DELIMITER, -1);
        if (parts.length >= 5) {
            Notification notification = new Notification();
            notification.setNotificationId(parts[0]);
            notification.setTargetUserId(parts[1]);
            if (!parts[2].isEmpty()) {
                notification.setDateTime(LocalDateTime.parse(parts[2], FORMATTER));
            }
            notification.setMessage(parts[3]);
            notification.setRead(Boolean.parseBoolean(parts[4]));
            return notification;
        }
        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String toString() {
        return "Notification [ID=" + this.notificationId + ", To=" + this.targetUserId
                + ", Message=" + this.message + ", Read=" + this.isRead + "]";
    }
}
