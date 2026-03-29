package ui;

import services.UserService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Customer self-registration screen.
 * Validates all fields and hashes the password before saving.
 */
public class RegisterFrame extends JFrame {

    private JTextField     tfUsername, tfName, tfEmail, tfPhone;
    private JPasswordField pfPassword, pfConfirm;
    private JLabel         lblError;

    public RegisterFrame() {
        setTitle("APU-ASC — Register");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 640);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

        JLabel title = UITheme.titleLabel("Create Account");
        title.setName("lblTitle");
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = UITheme.mutedLabel("Register as a new customer");
        sub.setName("lblSubtitle");
        sub.setAlignmentX(CENTER_ALIGNMENT);
        sub.setHorizontalAlignment(SwingConstants.CENTER);

        tfUsername = UITheme.styledTextField(20);
        tfUsername.setName("tfUsername");
        tfName     = UITheme.styledTextField(20);
        tfName.setName("tfName");
        tfEmail    = UITheme.styledTextField(20);
        tfEmail.setName("tfEmail");
        tfPhone    = UITheme.styledTextField(20);
        tfPhone.setName("tfPhone");
        pfPassword = UITheme.styledPasswordField(20);
        pfPassword.setName("pfPassword");
        pfConfirm  = UITheme.styledPasswordField(20);
        pfConfirm.setName("pfConfirm");

        lblError = new JLabel(" ");
        lblError.setName("lblError");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.ACCENT);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        JButton btnRegister = UITheme.accentButton("Register");
        btnRegister.setName("btnRegister");
        btnRegister.setAlignmentX(CENTER_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnRegister.addActionListener(e -> doRegister());

        JButton btnBack = new JButton("← Back to Login");
        btnBack.setName("btnBack");
        btnBack.setFont(UITheme.FONT_SMALL);
        btnBack.setForeground(UITheme.TEXT_MUTED);
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.setAlignmentX(CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(UITheme.formRow("Username *", tfUsername));
        card.add(Box.createVerticalStrut(10));
        card.add(UITheme.formRow("Full Name *", tfName));
        card.add(Box.createVerticalStrut(10));
        card.add(UITheme.formRow("Email *", tfEmail));
        card.add(Box.createVerticalStrut(10));
        card.add(UITheme.formRow("Phone", tfPhone));
        card.add(Box.createVerticalStrut(10));
        card.add(UITheme.formRow("Password *", pfPassword));
        card.add(Box.createVerticalStrut(10));
        card.add(UITheme.formRow("Confirm Password *", pfConfirm));
        card.add(Box.createVerticalStrut(16));
        card.add(lblError);
        card.add(Box.createVerticalStrut(8));
        card.add(btnRegister);
        card.add(Box.createVerticalStrut(12));
        card.add(btnBack);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(24, 24, 24, 24);
        add(card, gbc);
    }

    private void doRegister() {
        String username = tfUsername.getText().trim();
        String name     = tfName.getText().trim();
        String email    = tfEmail.getText().trim();
        String phone    = tfPhone.getText().trim();
        String password = new String(pfPassword.getPassword());
        String confirm  = new String(pfConfirm.getPassword());

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
