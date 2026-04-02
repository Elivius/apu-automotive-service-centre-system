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
        setSize(500, 580);
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

        JLabel title = UITheme.titleLabel("Edit Profile");
        title.setName("title");
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel roleHint = UITheme.mutedLabel("Role: " + currentUser.getRole() + "  •  ID: " + currentUser.getUserId());
        roleHint.setName("roleHint");
        roleHint.setAlignmentX(CENTER_ALIGNMENT);
        roleHint.setHorizontalAlignment(SwingConstants.CENTER);

        // Pre-fill
        tfName  = UITheme.styledTextField(20);
        tfName.setName("tfName");
        tfName.setText(currentUser.getName());
        tfEmail = UITheme.styledTextField(20);
        tfEmail.setName("tfEmail");
        tfEmail.setText(currentUser.getEmail());
        tfPhone = UITheme.styledTextField(20);
        tfPhone.setName("tfPhone");
        tfPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");

        // Password change section
        JLabel pwHeader = UITheme.headerLabel("Change Password (optional)");
        pwHeader.setName("pwHeader");
        pwHeader.setAlignmentX(LEFT_ALIGNMENT);
        pfOldPw = UITheme.styledPasswordField(20);
        pfOldPw.setName("pfOldPw");
        pfNewPw = UITheme.styledPasswordField(20);
        pfNewPw.setName("pfNewPw");
        pfConfirmPw = UITheme.styledPasswordField(20);
        pfConfirmPw.setName("pfConfirmPw");

        lblError = new JLabel(" ");
        lblError.setName("lblError");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.ACCENT);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        JButton btnSave = UITheme.accentButton("Save Changes");
        btnSave.setName("btnSave");
        btnSave.setAlignmentX(CENTER_ALIGNMENT);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnSave.addActionListener(e -> doSave());

        card.add(title);   
        card.add(Box.createVerticalStrut(4));
        card.add(roleHint); 
        card.add(Box.createVerticalStrut(24));
        card.add(UITheme.formRow("Full Name *", tfName));   
        card.add(Box.createVerticalStrut(10));
        card.add(UITheme.formRow("Email *",     tfEmail));  
        card.add(Box.createVerticalStrut(10));
        card.add(UITheme.formRow("Phone",       tfPhone));  
        card.add(Box.createVerticalStrut(20));

        JSeparator sep = new JSeparator(); 
        sep.setForeground(UITheme.FIELD_BORDER); 
        sep.setAlignmentX(CENTER_ALIGNMENT);
        card.add(sep); 
        card.add(Box.createVerticalStrut(12));

        card.add(pwHeader); 
        card.add(Box.createVerticalStrut(10));
        card.add(UITheme.formRow("Current Password", pfOldPw));     
        card.add(Box.createVerticalStrut(10));
        card.add(UITheme.formRow("New Password", pfNewPw));     
        card.add(Box.createVerticalStrut(10));
        card.add(UITheme.formRow("Confirm Password", pfConfirmPw));
        card.add(Box.createVerticalStrut(16));
        card.add(lblError);
        card.add(Box.createVerticalStrut(8));
        card.add(btnSave);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(24, 24, 24, 24);
        add(card, gbc);
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
            currentUser.setHashedPassword(newPw);
        }

        try {
            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            UserService.updateUser(currentUser);
            
//          Reflect on the dashboard
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
