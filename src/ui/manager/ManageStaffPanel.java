package ui.manager;

import models.User;
import services.UserService;
import ui.UITheme;
import utils.InputValidator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD panel for Manager, CounterStaff, and Technician accounts.
 * Uses a JTabbedPane — one tab per role.
 */
public class ManageStaffPanel extends JPanel {

    public ManageStaffPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.titleLabel("Manage Staff"), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UITheme.BG_CARD);
        tabs.setForeground(UITheme.TEXT_PRIMARY);
        tabs.setFont(UITheme.FONT_BODY);

        tabs.addTab("👔  Managers", buildRoleTab("Manager"));
        tabs.addTab("🧑‍💼  Counter Staff", buildRoleTab("CounterStaff"));
        tabs.addTab("🔧  Technicians", buildRoleTab("Technician"));

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildRoleTab(String role) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));

        boolean isTech = "Technician".equals(role);
        String[] cols = isTech
                ? new String[]{"User ID", "Name", "Username", "Email", "Phone", "Specialization"}
                : new String[]{"User ID", "Name", "Username", "Email", "Phone"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setName("tblStaff");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Search
        JTextField tfSearch = UITheme.styledTextField(16);
        tfSearch.setName("tfSearch");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() { 
                String text = tfSearch.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        JScrollPane sp = UITheme.styledTable(table);

        JButton btnAdd = UITheme.accentButton("+ Add");
        btnAdd.setName("btnAdd");
        JButton btnEdit = UITheme.warningButton("✏️  Edit");
        btnEdit.setName("btnEdit");
        JButton btnDelete  = UITheme.dangerButton("🗑  Delete");
        btnDelete.setName("btnDelete");
        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");

        // Holder for current list
        final java.util.List<User>[] userRef = new java.util.List[]{java.util.Collections.emptyList()};

        Runnable loadData = () -> {
            model.setRowCount(0);
            userRef[0] = UserService.getAllStaff().stream()
                    .filter(user -> role.equals(user.getRole())).collect(Collectors.toList());
            for (User user : userRef[0]) {
                if (isTech) {
                    models.Technician tech = (models.Technician) user;
                    model.addRow(new Object[]{user.getUserId(), user.getName(), user.getUsername(),
                        user.getEmail(), user.getPhone(), tech.getSpecialization()});
                } else {
                    model.addRow(new Object[]{user.getUserId(), user.getName(), user.getUsername(), user.getEmail(), user.getPhone()});
                }
            }
            if (tfSearch != null) {
                tfSearch.setText("");
            }
            if (sorter != null) {
                sorter.setRowFilter(null);
            }
        };
        loadData.run();

        btnRefresh.addActionListener(e -> loadData.run());
        btnAdd.addActionListener(e -> {
            showStaffForm(null, role, isTech);
            loadData.run();
        });
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
				JOptionPane.showMessageDialog(this, "Please select a user to edit.");
				return;
            };
            User user = userRef[0].get(table.convertRowIndexToModel(row));
            showStaffForm(user, role, isTech);
            loadData.run();
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
				JOptionPane.showMessageDialog(this, "Please select a user to delete.");
				return;
            }
            User user = userRef[0].get(table.convertRowIndexToModel(row));
            int ok = JOptionPane.showConfirmDialog(this,
                "Delete \"" + user.getName() + "\"?\nThis action cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) { UserService.deleteUser(user); loadData.run(); }
        });

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchRow.setOpaque(false);
        searchRow.add(new JLabel("🔍"));
        searchRow.add(tfSearch);
        topBar.add(searchRow, BorderLayout.WEST);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnAdd);
        btnRow.add(btnEdit);
        btnRow.add(btnDelete);
        btnRow.add(btnRefresh);
        topBar.add(btnRow, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private void showStaffForm(User prefill, String role, boolean isTech) {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.BG_CARD);

        JTextField tfUsername = UITheme.styledTextField(20);
        tfUsername.setName("tfUsername");
        JTextField tfName = UITheme.styledTextField(20);
        tfName.setName("tfName");
        JTextField tfEmail = UITheme.styledTextField(20);
        tfEmail.setName("tfEmail");
        JTextField tfPhone = UITheme.styledTextField(20);
        tfPhone.setName("tfPhone");
        JTextField tfSpecialization = UITheme.styledTextField(20);
        tfSpecialization.setName("tfSpecialization");
        JPasswordField pfPassword = UITheme.styledPasswordField(20);
        pfPassword.setName("pfPassword");
        JPasswordField pfConfirmPassword = UITheme.styledPasswordField(20);
        pfConfirmPassword.setName("pfConfirmPassword");

        if (prefill != null) {
            tfUsername.setText(prefill.getUsername());
            tfUsername.setEditable(false);
            tfName.setText(prefill.getName());
            tfEmail.setText(prefill.getEmail());
            tfPhone.setText(prefill.getPhone() != null ? prefill.getPhone() : "");
            if (isTech) tfSpecialization.setText(((models.Technician)prefill).getSpecialization());
        }

        form.add(UITheme.formRow("Username (View Only)", tfUsername));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Full Name *", tfName));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Email *", tfEmail));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Phone", tfPhone));
        form.add(Box.createVerticalStrut(8));
        if (isTech) {
        	form.add(UITheme.formRow("Specialization *", tfSpecialization));
        	form.add(Box.createVerticalStrut(8));
        }
        if (prefill == null) {
        	form.add(UITheme.formRow("Password *", pfPassword));
        	form.add(Box.createVerticalStrut(8));
        	form.add(UITheme.formRow("Confirm Password *", pfConfirmPassword));
        	form.add(Box.createVerticalStrut(8));
        }

        String title = (prefill == null ? "Add " : "Edit ") + role;
        int res = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
        	return;
        }

        if (prefill == null) {
            String password = new String(pfPassword.getPassword());
            String confirmPassword = new String(pfConfirmPassword.getPassword());
            String username = tfUsername.getText().trim();
            String name = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            String phone = tfPhone.getText().trim();
            String specialization = tfSpecialization.getText().trim();
            if (username.isEmpty() || name.isEmpty() || password.isEmpty() || email.isEmpty() || (isTech && specialization.isEmpty())) {
                if (isTech) {
                    JOptionPane.showMessageDialog(this, "Username, Name, Password, Email, Specialization are required.");
                } else {
                    JOptionPane.showMessageDialog(this, "Username, Name, Password, Email are required.");
                }
                return;
            }
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }
            if (!InputValidator.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format.");
                return;
            }
            if (!phone.isEmpty() && !InputValidator.isValidPhone(phone)) {
                JOptionPane.showMessageDialog(this, "Phone number must contain digits only.");
                return;
            }

            try {
                if (isTech) {
                    UserService.registerUser(username, password, name, email, phone, role, specialization);
                } else {
                    UserService.registerUser(username, password, name, email, phone, role);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                return;
            }
        } else {
            String name = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            String phone = tfPhone.getText().trim();
            String specialization = isTech ? tfSpecialization.getText().trim() : null;
            if (!InputValidator.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format.");
                return;
            }
            if (!phone.isEmpty() && !InputValidator.isValidPhone(phone)) {
                JOptionPane.showMessageDialog(this, "Phone number must contain digits only.");
                return;
            }
            try {
                UserService.updateStaffProfile(prefill, name, email, phone, specialization);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                return;
            }
        }
    }
}
