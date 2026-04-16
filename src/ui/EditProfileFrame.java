package ui;

import models.User;
import services.UserService;
import utils.PasswordHasher;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Edit-profile screen available to every role.
 * Pre-fills the current user's details.
 * Optionally changes the password (requires old password verification).
 */
public class EditProfileFrame extends JFrame {

    private final User currentUser;
    private Runnable onProfileUpdate;
    private JTextField tfName, tfEmail, tfPhone;
    private JPasswordField pfOldPw, pfNewPw, pfConfirmPw;
    private JLabel lblError;

    public EditProfileFrame(User user) {
        this(user, null);
    }

    public EditProfileFrame(User user, Runnable onProfileUpdate) {
        this.currentUser = user;
        this.onProfileUpdate = onProfileUpdate;
        setTitle("Edit Profile — " + user.getName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 680);
        setMinimumSize(new Dimension(480, 500));
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

        // ── Title area ───────────────────────────────────────────────
        JLabel title = UITheme.titleLabel("Edit Profile");
        title.setName("title");
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel roleHint = UITheme.mutedLabel("Role: " + currentUser.getRole() + "  •  ID: " + currentUser.getUserId());
        roleHint.setName("roleHint");
        roleHint.setAlignmentX(CENTER_ALIGNMENT);
        roleHint.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Fields (pre-filled) ──────────────────────────────────────
        tfName = UITheme.styledTextField(20);
        tfName.setName("tfName");
        tfName.setText(currentUser.getName());

        tfEmail = UITheme.styledTextField(20);
        tfEmail.setName("tfEmail");
        tfEmail.setText(currentUser.getEmail());

        tfPhone = UITheme.styledTextField(20);
        tfPhone.setName("tfPhone");
        tfPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");

        // ── Password change section ───────────────────────────────────
        JLabel pwHeader = UITheme.headerLabel("Change Password (optional)");
        pwHeader.setName("pwHeader");
        pwHeader.setAlignmentX(CENTER_ALIGNMENT);
        pwHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        pfOldPw = UITheme.styledPasswordField(20);
        pfOldPw.setName("pfOldPw");
        pfNewPw = UITheme.styledPasswordField(20);
        pfNewPw.setName("pfNewPw");
        pfConfirmPw = UITheme.styledPasswordField(20);
        pfConfirmPw.setName("pfConfirmPw");

        lblError = new JLabel(" ");
        lblError.setName("lblError");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        JButton btnSave = UITheme.accentButton("Save Changes");
        btnSave.setName("btnSave");
        btnSave.setAlignmentX(CENTER_ALIGNMENT);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnSave.addActionListener(e -> doSave());

        // ── Assembly ─────────────────────────────────────────────────
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(roleHint);
        card.add(Box.createVerticalStrut(28));
        card.add(UITheme.formRow("Full Name *", tfName));
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("Email *", tfEmail));
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("Phone", tfPhone));
        card.add(Box.createVerticalStrut(20));

        JSeparator sep = UITheme.sectionDivider();
        sep.setAlignmentX(CENTER_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(14));

        card.add(pwHeader);
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("Current Password", pfOldPw));
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("New Password", pfNewPw));
        card.add(Box.createVerticalStrut(12));
        card.add(UITheme.formRow("Confirm Password", pfConfirmPw));
        card.add(Box.createVerticalStrut(18));
        card.add(lblError);
        card.add(Box.createVerticalStrut(8));
        card.add(btnSave);

        // ── Scroll wrapper ─────────────
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

    private void doSave() {
        String name = tfName.getText().trim();
        String email = tfEmail.getText().trim();
        String phone = tfPhone.getText().trim();
        String oldPw = new String(pfOldPw.getPassword());
        String newPw = new String(pfNewPw.getPassword());
        String confPw = new String(pfConfirmPw.getPassword());

        if (name.isEmpty() || email.isEmpty()) {
            lblError.setText("Name and email are required.");
            return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            lblError.setText("Invalid email format.");
            return;
        }

        // Password change (only if any password field is filled)
        boolean changingPassword = !oldPw.isEmpty() || !newPw.isEmpty();
        if (changingPassword) {
            if (!PasswordHasher.verify(oldPw, currentUser.getPassword())) {
                lblError.setText("Current password is incorrect.");
                return;
            }
            if (newPw.length() < 6) {
                lblError.setText("New password must be at least 6 characters.");
                return;
            }
            if (!newPw.equals(confPw)) {
                lblError.setText("New passwords do not match.");
                return;
            }
        }

        try {
            UserService.updateUserProfile(currentUser, name, email, phone, changingPassword ? newPw : null);

            // Reflect name change on the dashboard
            if (onProfileUpdate != null) {
                onProfileUpdate.run();
            }

            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException ex) {
            lblError.setText(ex.getMessage());
        }
    }
}
