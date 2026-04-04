package services;

import models.Notification;
import utils.FileHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralized service for managing system notifications.
 * All other services call this class to push notifications,
 * keeping notification logic in a single place (DRY Principle).
 *
 * Notifications are stored in notifications.txt and displayed
 * via the notification bell icon on each dashboard.
 *
 * Read tracking strategy:
 * - Direct notifications (target = specific userId): flip isRead on the record.
 * - Role/broadcast notifications (target = role name or "ALL"): track per-user
 *   in notification_reads.txt so one user's read action doesn't hide it from others.
 */
public class NotificationService {

    /**
     * Pushes a notification to a specific user (Store in notification.txt first).
     *
     * @param targetUserId the user ID to notify (e.g., "CUS0001", "TEC0002")
     *                     Use "ALL" for broadcast to everyone.
     *                     Use a role name (e.g., "CounterStaff") for role-wide broadcast.
     * @param message      the notification message
     */
    public static void push(String targetUserId, String message) {
        String newId = FileHandler.getInstance().generateNextId(FileHandler.NOTIFICATIONS_FILE, "NTF");

        Notification notification = new Notification(newId, targetUserId, message, LocalDateTime.now(), false);
        FileHandler.getInstance().appendLine(FileHandler.NOTIFICATIONS_FILE, notification.toFileString());
    }

    /**
     * Overloaded push method that takes a User object.
     * Extracts the userId automatically.
     * 
     * @param user    the user to notify
     * @param message the notification message
     */
    public static void push(models.User user, String message) {
        if (user != null && user.getUserId() != null) {
            push(user.getUserId(), message);
        }
    }

    /**
     * Retrieves all unread notifications for a specific user.
     * Also includes broadcast ("ALL") and role-based notifications
     * that this user has not yet personally dismissed.
     *
     * @param userId the user's ID (e.g., "CUS0001")
     * @param role   the user's role (e.g., "CounterStaff")
     * @return a list of Notification objects for this user
     */
    public static List<Notification> getNotificationsForUser(String userId, String role) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.NOTIFICATIONS_FILE);
        // Set that contains all the notification IDs that the user has read
        Set<String> userReads = getReadNotificationIds(userId);

        return lines.stream()
                .map(Notification::fromFileString)
                .filter(notification -> notification != null)
                .filter(notification -> isNotificationForUser(notification, userId, role))
                .filter(notification -> {
                    if (isDirectNotification(notification, userId)) {
                        // Direct notification: use the isRead flag on the record - notification.txt
                        return !notification.isRead();
                    } else {
                        // Role/broadcast: check per-user reads file - notification_reads.txt
                        return !userReads.contains(notification.getNotificationId());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Marks a specific notification as read for a given user.
     * - Direct notifications: flips isRead on the shared record.
     * - Role/broadcast notifications: records per-user read in notification_reads.txt.
     *
     * @param notification the Notification object to mark as read
     * @param userId       the user's ID (e.g., "CUS0001")
     * @param role         the user's role (e.g., "CounterStaff")
     */
    public static void markAsRead(Notification notification, String userId, String role) {
        if (notification == null || notification.getNotificationId() == null) {
            return;
        }
        if (!isNotificationForUser(notification, userId, role)) {
            return;
        }

        if (isDirectNotification(notification, userId)) {
            // Direct notification — flip the flag on the record itself
            notification.setRead(true);
            FileHandler.getInstance().updateLine(FileHandler.NOTIFICATIONS_FILE, notification.getNotificationId(), notification.toFileString());
        } else {
            // Role/broadcast — record per-user read (don't touch the shared record)
            String readEntry = notification.getNotificationId() + FileHandler.SEPARATOR + userId;
            FileHandler.getInstance().appendLine(FileHandler.NOTIFICATION_READS_FILE, readEntry);
        }
    }

    /**
     * Marks all notifications for a specific user as read.
     * Called when a user clicks "Mark All as Read" in the notification panel.
     *
     * @param userId the user's ID
     * @param role   the user's role (e.g., "CounterStaff")
     */
    public static void markAllAsRead(String userId, String role) {
        List<Notification> unreadNotifications = getNotificationsForUser(userId, role);
        for (Notification notification : unreadNotifications) {
            markAsRead(notification, userId, role);
        }
    }

    /**
     * Returns the count of unread notifications for a user.
     * Useful for the bell icon badge.
     *
     * @param userId the user's ID
     * @param role   the user's role
     * @return the number of unread notifications
     */
    public static int getUnreadCount(String userId, String role) {
        return getNotificationsForUser(userId, role).size();
    }

    // ═══════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════

    /**
     * Checks whether a notification targets a specific user.
     * Matches by direct userId, role, or broadcast ("ALL").
     */
    private static boolean isNotificationForUser(Notification notification, String userId, String role) {
        String target = notification.getTargetUserId();
        return target.equals(userId) || target.equals(role) || target.equals("ALL");
    }

    /**
     * Checks whether a notification is a direct (1-to-1) notification for a specific user.
     * Returns false for role-based or broadcast notifications.
     */
    private static boolean isDirectNotification(Notification notification, String userId) {
        return notification.getTargetUserId().equals(userId);
    }

    /**
     * Loads the set of notification IDs that a specific user has already read.
     * Reads from notification_reads.txt.
     *
     * @param userId the user's ID
     * @return a set of notification IDs the user has read
     */
    private static Set<String> getReadNotificationIds(String userId) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.NOTIFICATION_READS_FILE);
        Set<String> readIds = new HashSet<>();
        for (String line : lines) {
            String[] parts = line.split(FileHandler.DELIMITER, -1);
            if (parts.length >= 2 && parts[1].equals(userId)) {
                readIds.add(parts[0]);
            }
        }
        return readIds;
    }
}
