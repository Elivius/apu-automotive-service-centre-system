package ui;

import models.User;
import models.Notification;
import services.NotificationService;
import services.UserService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Login screen — entry point for all users.
 *
 * On successful login:
 *   1. Fires ToastNotification for each unread notification.
 *   2. Calls user.displayDashboard() via polymorphism to open the right JFrame.
 */
public class LoginFrame extends JFrame {

    private JTextField     tfUsername;
    private JPasswordField pfPassword;
    private JLabel         lblError;

    public LoginFrame() {
        setTitle("APU Automotive Service Centre — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 44, 40, 44));

        // ── Logo / Title ─────────────────────────────────────────────
        JLabel logo = new JLabel("🔧 APU-ASC", SwingConstants.CENTER);
        logo.setName("lblLogo");
        logo.setFont(UITheme.FONT_TITLE);
        logo.setForeground(UITheme.ACCENT);
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = UITheme.mutedLabel("Automotive Service Centre");
        subtitle.setName("lblSubtitle");
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Fields ───────────────────────────────────────────────────
        tfUsername = UITheme.styledTextField(20);
        tfUsername.setName("tfUsername"); // Kinda like HTML id="tfUsername" for automated test locating
        pfPassword = UITheme.styledPasswordField(20);
        pfPassword.setName("pfPassword");

        // ── Error label ──────────────────────────────────────────────
        lblError = new JLabel(" ");
        lblError.setName("lblError");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.ACCENT);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        // ── Login button ─────────────────────────────────────────────
        JButton btnLogin = UITheme.accentButton("Login");
        btnLogin.setName("btnLogin");
        btnLogin.setAlignmentX(CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.addActionListener(e -> doLogin());
        pfPassword.addActionListener(e -> doLogin()); // Enter key

        // ── Register link ─────────────────────────────────────────────
        JButton btnRegister = new JButton("New customer? Register here");
        btnRegister.setName("btnRegister");
        btnRegister.setFont(UITheme.FONT_SMALL);
        btnRegister.setForeground(UITheme.TEXT_MUTED);
        btnRegister.setBorderPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRegister.setAlignmentX(CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        // ── Assemble ──────────────────────────────────────────────────
        card.add(logo);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(32));
        card.add(UITheme.formRow("Username", tfUsername));
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("Password", pfPassword));
        card.add(Box.createVerticalStrut(20));
        card.add(lblError);
        card.add(Box.createVerticalStrut(8));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));
        card.add(btnRegister);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(24, 24, 24, 24);
        add(card, gbc);
    }

    private void doLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter your username and password.");
            return;
        }

        User user = UserService.loginUser(username, password);
        if (user == null) {
            lblError.setText("Invalid username or password.");
            pfPassword.setText("");
            return;
        }

        // Show unread notifications as toast popups
        List<Notification> notifications =
                NotificationService.getNotificationsForUser(user.getUserId(), user.getRole());
        for (Notification n : notifications) {
            ToastNotification.show(n.getMessage());
            NotificationService.markAsRead(n, user.getUserId());
        }

        // Polymorphism: open the role-specific dashboard
        dispose();
        user.displayDashboard();
    }
}
