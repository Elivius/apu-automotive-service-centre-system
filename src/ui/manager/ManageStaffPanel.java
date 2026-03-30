package ui.manager;

import models.User;
import services.UserService;
import ui.UITheme;

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
            void filter() { String text = tfSearch.getText().trim();
                sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 1)); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        JScrollPane sp = UITheme.styledTable(table);

        JButton btnRefresh = UITheme.secondaryButton("↻");
        btnRefresh.setName("btnRefresh");
        JButton btnAdd     = UITheme.accentButton("+ Add");
        btnAdd.setName("btnAdd");
        JButton btnEdit    = UITheme.secondaryButton("✏️  Edit");
        btnEdit.setName("btnEdit");
        JButton btnDelete  = UITheme.dangerButton("🗑  Delete");
        btnDelete.setName("btnDelete");

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
        };
        loadData.run();

        btnRefresh.addActionListener(e -> loadData.run());
        btnAdd.addActionListener(e -> {
            showStaffForm(null, role, isTech);
            loadData.run();
        });
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row < 0) return;
            User user = userRef[0].get(table.convertRowIndexToModel(row));
            showStaffForm(user, role, isTech);
            loadData.run();
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row < 0) return;
            User user = userRef[0].get(table.convertRowIndexToModel(row));
            int ok = JOptionPane.showConfirmDialog(this,
                "Delete \"" + user.getName() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
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
        panel.add(sp,     BorderLayout.CENTER);
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

        if (prefill != null) {
            tfUsername.setText(prefill.getUsername());
            tfUsername.setEditable(false);
            tfName.setText(prefill.getName());
            tfEmail.setText(prefill.getEmail());
            tfPhone.setText(prefill.getPhone() != null ? prefill.getPhone() : "");
            if (isTech) tfSpecialization.setText(((models.Technician)prefill).getSpecialization());
        }

        form.add(UITheme.formRow("Username *", tfUsername));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Full Name *", tfName));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Email", tfEmail));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Phone", tfPhone));
        form.add(Box.createVerticalStrut(8));
        if (isTech) { form.add(UITheme.formRow("Specialization", tfSpecialization));
        form.add(Box.createVerticalStrut(8)); }
        if (prefill == null) { form.add(UITheme.formRow("Password *", pfPassword)); }

        String title = (prefill == null ? "Add " : "Edit ") + role;
        int res = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        if (prefill == null) {
            String pw = new String(pfPassword.getPassword());
            String username = tfUsername.getText().trim();
            String name = tfName.getText().trim();
            if (username.isEmpty() || name.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username, Name, Password are required.");
                return;
            }
            if (isTech) {
                UserService.registerUser(username, pw, name, tfEmail.getText().trim(), tfPhone.getText().trim(), role, tfSpecialization.getText().trim());
            } else {
                UserService.registerUser(username, pw, name, tfEmail.getText().trim(), tfPhone.getText().trim(), role);
            }
        } else {
            try {
                prefill.setName(tfName.getText().trim());
                prefill.setEmail(tfEmail.getText().trim());
                prefill.setPhone(tfPhone.getText().trim());
                if (isTech) ((models.Technician)prefill).setSpecialization(tfSpecialization.getText().trim());
                UserService.updateUser(prefill);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
}
