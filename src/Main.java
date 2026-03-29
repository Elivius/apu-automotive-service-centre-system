import ui.LoginFrame;
import ui.UITheme;

import javax.swing.*;

/**
 * Application entry point for APU Automotive Service Centre.
 *
 * Applies the global dark theme, then opens the Login screen on the
 * Swing Event Dispatch Thread (EDT). From there, polymorphism handles
 * routing each user to their role-specific dashboard.
 */
public class Main {
    public static void main(String[] args) {
        // Apply look-and-feel BEFORE any Frame is created
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Apply global dark theme defaults
        UITheme.applyGlobalDefaults();

        // Launch on the EDT
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
