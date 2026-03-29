package ui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Self-fading "Toast" notification popup.
 *
 * Appears bottom-right of the screen and fades out after 3 seconds.
 * Implements a queue so multiple toasts stack without overlapping.
 *
 * Usage:
 *   ToastNotification.show("Your appointment has been assigned.");
 */
public class ToastNotification extends JWindow {

    private static final int TOAST_WIDTH   = 340;
    private static final int TOAST_HEIGHT  = 60;
    private static final int MARGIN        = 16;   // gap from screen edge & between toasts
    private static final int DISPLAY_MS    = 3000;
    private static final int FADE_STEP_MS  = 40;
    private static final float FADE_STEP_AMT = 0.05f;

    // Queue to track stacked toasts
    private static final Queue<ToastNotification> activeToasts = new LinkedList<>();

    private final String message;
    private float opacity = 0f;

    private ToastNotification(String message) {
        this.message = message;
        setSize(TOAST_WIDTH, TOAST_HEIGHT);
        setAlwaysOnTop(true);
        setOpacity(0f);

        JPanel content = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SIDEBAR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UITheme.ACCENT);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // Bell icon label
        JLabel icon = new JLabel("🔔");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 18));
        content.add(icon, BorderLayout.WEST);

        JLabel msg = new JLabel("<html><body style='width:220px'>" + message + "</body></html>");
        msg.setFont(UITheme.FONT_SMALL);
        msg.setForeground(UITheme.TEXT_PRIMARY);
        content.add(msg, BorderLayout.CENTER);

        add(content);
    }

    /**
     * Shows a toast notification. Thread-safe, can be called from any thread.
     *
     * @param message the text to display
     */
    public static void show(String message) {
        SwingUtilities.invokeLater(() -> {
            ToastNotification toast = new ToastNotification(message);
            activeToasts.add(toast);
            toast.positionAndDisplay();
        });
    }

    private void positionAndDisplay() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screen.width  - TOAST_WIDTH  - MARGIN;
        // Stack above existing toasts
        int stackOffset = activeToasts.stream()
                .filter(t -> t != this && t.isVisible())
                .mapToInt(t -> TOAST_HEIGHT + MARGIN)
                .sum();
        int y = screen.height - TOAST_HEIGHT - MARGIN - stackOffset;
        setLocation(x, y);
        setVisible(true);

        // Fade in
        Timer fadeIn = new Timer(FADE_STEP_MS, null);
        fadeIn.addActionListener(e -> {
            opacity = Math.min(1f, opacity + FADE_STEP_AMT * 2);
            setOpacity(opacity);
            if (opacity >= 1f) fadeIn.stop();
        });
        fadeIn.start();

        // Hold then fade out
        Timer hold = new Timer(DISPLAY_MS, e -> {
            Timer fadeOut = new Timer(FADE_STEP_MS, null);
            fadeOut.addActionListener(e2 -> {
                opacity = Math.max(0f, opacity - FADE_STEP_AMT);
                setOpacity(opacity);
                if (opacity <= 0f) {
                    fadeOut.stop();
                    dispose();
                    activeToasts.remove(this);
                }
            });
            fadeOut.start();
        });
        hold.setRepeats(false);
        hold.start();
    }
}
