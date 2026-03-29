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

        tabs.addTab("👔  Managers",      buildRoleTab("Manager"));
        tabs.addTab("🧑‍💼  Counter Staff", buildRoleTab("CounterStaff"));
        tabs.addTab("🔧  Technicians",   buildRoleTab("Technician"));

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
            void filter() { String t = tfSearch.getText().trim();
                sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t, 1)); }
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
            List<String> lines = utils.FileHandler.getInstance().readAllLines(utils.FileHandler.USERS_FILE);
            userRef[0] = lines.stream().map(UserService::parseUser)
                    .filter(u -> u != null && role.equals(u.getRole())).collect(Collectors.toList());
            for (User u : userRef[0]) {
                if (isTech) {
                    models.Technician t = (models.Technician) u;
                    model.addRow(new Object[]{u.getUserId(), u.getName(), u.getUsername(),
                        u.getEmail(), u.getPhone(), t.getSpecialization()});
                } else {
                    model.addRow(new Object[]{u.getUserId(), u.getName(), u.getUsername(), u.getEmail(), u.getPhone()});
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
            User u = userRef[0].get(table.convertRowIndexToModel(row));
            showStaffForm(u, role, isTech);
            loadData.run();
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row < 0) return;
            User u = userRef[0].get(table.convertRowIndexToModel(row));
            int ok = JOptionPane.showConfirmDialog(this,
                "Delete \"" + u.getName() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) { UserService.deleteUser(u); loadData.run(); }
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

        JTextField tfU = UITheme.styledTextField(20);
        tfU.setName("tfUsername");
        JTextField tfN = UITheme.styledTextField(20);
        tfN.setName("tfName");
        JTextField tfE = UITheme.styledTextField(20);
        tfE.setName("tfEmail");
        JTextField tfP = UITheme.styledTextField(20);
        tfP.setName("tfPhone");
        JTextField tfSpec = UITheme.styledTextField(20);
        tfSpec.setName("tfSpecialization");
        JPasswordField pfPw = UITheme.styledPasswordField(20);
        pfPw.setName("pfPassword");

        if (prefill != null) {
            tfU.setText(prefill.getUsername());
            tfU.setEditable(false);
            tfN.setText(prefill.getName());
            tfE.setText(prefill.getEmail());
            tfP.setText(prefill.getPhone() != null ? prefill.getPhone() : "");
            if (isTech) tfSpec.setText(((models.Technician)prefill).getSpecialization());
        }

        form.add(UITheme.formRow("Username *", tfU));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Full Name *", tfN));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Email",       tfE));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Phone",       tfP));
        form.add(Box.createVerticalStrut(8));
        if (isTech) { form.add(UITheme.formRow("Specialization", tfSpec));
        form.add(Box.createVerticalStrut(8)); }
        if (prefill == null) { form.add(UITheme.formRow("Password *", pfPw)); }

        String title = (prefill == null ? "Add " : "Edit ") + role;
        int res = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        if (prefill == null) {
            String pw = new String(pfPw.getPassword());
            String username = tfU.getText().trim();
            String name     = tfN.getText().trim();
            if (username.isEmpty() || name.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username, Name, Password are required.");
                return;
            }
            if (isTech) {
                UserService.registerUser(username, pw, name, tfE.getText().trim(), tfP.getText().trim(), role, tfSpec.getText().trim());
            } else {
                UserService.registerUser(username, pw, name, tfE.getText().trim(), tfP.getText().trim(), role);
            }
        } else {
            try {
                prefill.setName(tfN.getText().trim());
                prefill.setEmail(tfE.getText().trim());
                prefill.setPhone(tfP.getText().trim());
                if (isTech) ((models.Technician)prefill).setSpecialization(tfSpec.getText().trim());
                UserService.updateUser(prefill);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
}
