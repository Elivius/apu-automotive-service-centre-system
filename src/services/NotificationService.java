package services;

import models.Notification;
import utils.FileHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized service for managing system notifications.
 * All other services call this class to push notifications,
 * keeping notification logic in a single place (DRY Principle).
 *
 * Notifications are stored in notifications.txt and displayed
 * as pop-ups when a user logs in or performs certain actions.
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
     * Also includes broadcast ("ALL") and role-based notifications.
     *
     * @param userId the user's ID (e.g., "CUS0001")
     * @param role   the user's role (e.g., "CounterStaff")
     * @return a list of Notification objects for this user
     */
    public static List<Notification> getNotificationsForUser(String userId, String role) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.NOTIFICATIONS_FILE);
        return lines.stream()
                .map(Notification::fromFileString)
                .filter(n -> n != null && !n.isRead())
                .filter(n -> n.getTargetUserId().equals(userId) ||
                             n.getTargetUserId().equals(role) ||
                             n.getTargetUserId().equals("ALL"))
                .collect(Collectors.toList());
    }

    /**
     * Marks a specific notification as read.
     *
     * @param notification the Notification object to mark as read
     * @param userId the user's ID (e.g., "CUS0001")
     */
    public static void markAsRead(Notification notification, String userId) {
        if (notification != null && notification.getNotificationId() != null && notification.getTargetUserId().equals(userId)) {
            notification.setRead(true);
            FileHandler.getInstance().updateLine(FileHandler.NOTIFICATIONS_FILE, notification.getNotificationId(), notification.toFileString());
        }
    }

    /**
     * Marks all notifications for a specific user as read.
     * Called when a user views their notification panel.
     *
     * @param userId the user's ID
     */
    public static void markAllAsRead(String userId) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.NOTIFICATIONS_FILE);
        List<String> updatedLines = new ArrayList<>();
        for (String line : lines) {
            Notification notification = Notification.fromFileString(line);
            if (notification != null && notification.getTargetUserId().equals(userId)) {
                notification.setRead(true);
                updatedLines.add(notification.toFileString());
            } else {
                updatedLines.add(line);
            }
        }
        FileHandler.getInstance().writeAllLines(FileHandler.NOTIFICATIONS_FILE, updatedLines);
    }
}
