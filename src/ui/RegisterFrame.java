package ui;

import services.UserService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Customer self-registration screen.
 * Validates all fields and hashes the password before saving.
 *
 * Wrapped in a JScrollPane so all fields + both buttons are always reachable.
 */
public class RegisterFrame extends JFrame {

    private JTextField     tfUsername, tfName, tfEmail, tfPhone;
    private JPasswordField pfPassword, pfConfirm;
    private JLabel         lblError;

    public RegisterFrame() {
        setTitle("APU-ASC — Register");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 720);
        setMinimumSize(new Dimension(480, 540));
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(36, 40, 40, 40));

        // ── Title ─────────────────────────────────────────────────────
        JLabel title = UITheme.titleLabel("Create Account");
        title.setName("lblTitle");
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = UITheme.mutedLabel("Register as a new customer");
        sub.setName("lblSubtitle");
        sub.setAlignmentX(CENTER_ALIGNMENT);
        sub.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Fields ────────────────────────────────────────────────────
        tfUsername = UITheme.styledTextField(20);
        tfUsername.setName("tfUsername");
        tfName = UITheme.styledTextField(20);
        tfName.setName("tfName");
        tfEmail = UITheme.styledTextField(20);
        tfEmail.setName("tfEmail");
        tfPhone = UITheme.styledTextField(20);
        tfPhone.setName("tfPhone");
        pfPassword = UITheme.styledPasswordField(20);
        pfPassword.setName("pfPassword");
        pfConfirm = UITheme.styledPasswordField(20);
        pfConfirm.setName("pfConfirm");

        // ── Error label ───────────────────────────────────────────────
        lblError = new JLabel(" ");
        lblError.setName("lblError");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        // ── Register button ───────────────────────────────────────────
        JButton btnRegister = UITheme.accentButton("Create Account");
        btnRegister.setName("btnRegister");
        btnRegister.setAlignmentX(CENTER_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnRegister.addActionListener(e -> doRegister());

        // ── Back to Login link ────────────────────────────────────────
        JButton btnBack = new JButton("← Back to Login");
        btnBack.setName("btnBack");
        btnBack.setFont(UITheme.FONT_SMALL);
        btnBack.setForeground(UITheme.ACCENT_SECONDARY);
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.setAlignmentX(CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        // ── Assembly ──────────────────────────────────────────────────
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(28));

        card.add(UITheme.formRow("Username *", tfUsername));
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("Full Name *", tfName));
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("Email *", tfEmail));
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("Phone", tfPhone));
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("Password *", pfPassword));
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("Confirm Password *", pfConfirm));

        card.add(Box.createVerticalStrut(18));
        card.add(lblError);
        card.add(Box.createVerticalStrut(10));

        // Register button — same margin treatment as Login button
        card.add(btnRegister);
        card.add(Box.createVerticalStrut(16));   // breathing room below button

        // Divider to visually separate the back link
        JSeparator sepLink = UITheme.sectionDivider();
        sepLink.setAlignmentX(CENTER_ALIGNMENT);
        card.add(sepLink);
        card.add(Box.createVerticalStrut(12));

        card.add(btnBack);
        card.add(Box.createVerticalStrut(4));    // a bit of bottom padding

        // ── Scroll wrapper ────────────────────────────────────────────
        // The card fills horizontally (HORIZONTAL fill + NORTH anchor) so
        // BoxLayout calculates its natural height. The scroll pane ensures
        // both buttons are always reachable.
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(20, 20, 20, 20);
        outer.add(card, gbc);

        JScrollPane scroller = new JScrollPane(outer);
        scroller.setBorder(null);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.getVerticalScrollBar().setUnitIncrement(16);
        scroller.getViewport().setBackground(UITheme.BG_DARK);

        add(scroller, BorderLayout.CENTER);
    }

    private void doRegister() {
        String username = tfUsername.getText().trim();
        String name = tfName.getText().trim();
        String email = tfEmail.getText().trim();
        String phone = tfPhone.getText().trim();
        String password = new String(pfPassword.getPassword());
        String confirm = new String(pfConfirm.getPassword());

        if (username.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            lblError.setText("Please fill in all required (*) fields.");
            return;
        }
        if (!password.equals(confirm)) {
            lblError.setText("Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            lblError.setText("Password must be at least 6 characters.");
            return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            lblError.setText("Invalid email format.");
            return;
        }
        if (!phone.isEmpty() && !phone.matches("\\d+")) {
            lblError.setText("Phone number must contain digits only.");
            return;
        }

        try {
            UserService.registerUser(username, password, name, email, phone, "Customer");
            JOptionPane.showMessageDialog(this,
                "Account created! You can now log in.", "Success",
                JOptionPane.INFORMATION_MESSAGE);
            new LoginFrame().setVisible(true);
            dispose();
        } catch (Exception ex) {
            lblError.setText("Error: " + ex.getMessage());
        }
    }
}
