package ui;

import models.Notification;
import models.User;
import services.NotificationService;
import utils.DateUtils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * A dropdown notification panel that appears when the bell icon is clicked.
 * Displays unread notifications with dismiss buttons and a "Mark All as Read" action.
 *
 * Usage:
 *   NotificationPanel.show(bellButton, user, () -> updateBadge());
 */
public class NotificationPanel extends JPopupMenu {

    private static final int PANEL_WIDTH = 380;
    private static final int MAX_VISIBLE_HEIGHT = 420;

    private final User user;
    private final Runnable onDismissCallback;

    private NotificationPanel(User user, Runnable onDismissCallback) {
        this.user = user;
        this.onDismissCallback = onDismissCallback;
        setBackground(UITheme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.FIELD_BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        setPreferredSize(null); // will be set dynamically
        buildContent();
    }

    /**
     * Shows the notification panel as a popup anchored below the given component.
     *
     * @param anchor            the component to anchor the popup to (bell button)
     * @param user              the logged-in user
     * @param onDismissCallback callback invoked when any notification is dismissed
     */
    public static void show(Component anchor, User user, Runnable onDismissCallback) {
        NotificationPanel panel = new NotificationPanel(user, onDismissCallback);
        panel.show(anchor, anchor.getWidth() - PANEL_WIDTH, anchor.getHeight() + 4);
    }

    private void buildContent() {
        removeAll();
        setLayout(new BorderLayout());

        List<Notification> notifications = NotificationService.getNotificationsForUser(user.getUserId(), user.getRole());

        // ── Header ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_SIDEBAR);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel lblTitle = new JLabel("🔔  Notifications");
        lblTitle.setName("lblNotificationTitle");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);
        header.add(lblTitle, BorderLayout.WEST);

        if (!notifications.isEmpty()) {
            JLabel lblMarkAll = new JLabel("Mark all as read");
            lblMarkAll.setName("lblMarkAllRead");
            lblMarkAll.setFont(UITheme.FONT_SMALL);
            lblMarkAll.setForeground(UITheme.ACCENT);
            lblMarkAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lblMarkAll.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    NotificationService.markAllAsRead(user.getUserId(), user.getRole());
                    setVisible(false);
                    if (onDismissCallback != null) {
                        onDismissCallback.run();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    lblMarkAll.setForeground(UITheme.ACCENT_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lblMarkAll.setForeground(UITheme.ACCENT);
                }
            });
            header.add(lblMarkAll, BorderLayout.EAST);
        }

        add(header, BorderLayout.NORTH);

        // ── Notification List ─────────────────────────────────────────────
        if (notifications.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(UITheme.BG_CARD);
            emptyPanel.setPreferredSize(new Dimension(PANEL_WIDTH, 100));

            JLabel lblEmpty = new JLabel("No new notifications");
            lblEmpty.setName("lblNoNotifications");
            lblEmpty.setFont(UITheme.FONT_BODY);
            lblEmpty.setForeground(UITheme.TEXT_MUTED);
            emptyPanel.add(lblEmpty);

            add(emptyPanel, BorderLayout.CENTER);
        } else {
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setBackground(UITheme.BG_CARD);

            for (Notification notification : notifications) {
                listPanel.add(createNotificationRow(notification));
            }

            JScrollPane scrollPane = new JScrollPane(listPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(UITheme.BG_CARD);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar().setUnitIncrement(12);

            // Cap height
            int listHeight = Math.min(notifications.size() * 76, MAX_VISIBLE_HEIGHT);
            scrollPane.setPreferredSize(new Dimension(PANEL_WIDTH, listHeight));

            add(scrollPane, BorderLayout.CENTER);
        }

        // ── Footer (count) ────────────────────────────────────────────────
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(0x0D1B3E));
        footer.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        JLabel lblCount = new JLabel(notifications.size() + " unread notification"
                + (notifications.size() != 1 ? "s" : ""));
        lblCount.setName("lblNotificationCount");
        lblCount.setFont(UITheme.FONT_SMALL);
        lblCount.setForeground(UITheme.TEXT_MUTED);
        footer.add(lblCount, BorderLayout.WEST);

        add(footer, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    /**
     * Creates a single notification row with message, time, and dismiss button.
     */
    private JPanel createNotificationRow(Notification notification) {
        JPanel row = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Bottom border
                g.setColor(UITheme.FIELD_BORDER);
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        row.setBackground(UITheme.BG_CARD);
        row.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        // Left: accent indicator dot
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 8));
        dot.setForeground(UITheme.ACCENT);
        dot.setVerticalAlignment(SwingConstants.TOP);
        dot.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        row.add(dot, BorderLayout.WEST);

        // Center: message + time
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblMessage = new JLabel("<html><body style='width:260px'>"
                + notification.getMessage() + "</body></html>");
        lblMessage.setFont(UITheme.FONT_BODY);
        lblMessage.setForeground(UITheme.TEXT_PRIMARY);
        lblMessage.setAlignmentX(LEFT_ALIGNMENT);
        textPanel.add(lblMessage);

        textPanel.add(Box.createVerticalStrut(3));

        String timeStr = notification.getDateTime() != null
                ? notification.getDateTime().format(DateUtils.FORMATTER) : "";
        JLabel lblTime = new JLabel(timeStr);
        lblTime.setFont(UITheme.FONT_SMALL);
        lblTime.setForeground(UITheme.TEXT_MUTED);
        lblTime.setAlignmentX(LEFT_ALIGNMENT);
        textPanel.add(lblTime);

        row.add(textPanel, BorderLayout.CENTER);

        // Right: dismiss button
        JLabel btnDismiss = new JLabel("✕");
        btnDismiss.setName("btnDismiss_" + notification.getNotificationId());
        btnDismiss.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnDismiss.setForeground(UITheme.TEXT_MUTED);
        btnDismiss.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDismiss.setVerticalAlignment(SwingConstants.TOP);
        btnDismiss.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 4));
        btnDismiss.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NotificationService.markAsRead(notification, user.getUserId(), user.getRole());
                buildContent(); // rebuild the list
                if (onDismissCallback != null) {
                    onDismissCallback.run();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                btnDismiss.setForeground(UITheme.ACCENT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnDismiss.setForeground(UITheme.TEXT_MUTED);
            }
        });
        row.add(btnDismiss, BorderLayout.EAST);

        // Hover effect
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(UITheme.TABLE_ALT_ROW);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(UITheme.BG_CARD);
            }
        });

        return row;
    }
}
