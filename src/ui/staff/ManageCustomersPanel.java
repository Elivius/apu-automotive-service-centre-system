package ui.staff;

import models.CounterStaff;
import models.User;
import services.UserService;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD panel for managing Customer accounts.
 * Features live search (RowFilter) on the customer name column.
 */
public class ManageCustomersPanel extends JPanel {

    private final CounterStaff staff;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfSearch;
    private List<User> customers;

    public ManageCustomersPanel(CounterStaff staff) {
        this.staff = staff;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        // ── Header ────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(UITheme.titleLabel("Manage Customers"), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        tfSearch = UITheme.styledTextField(18);
        tfSearch.setToolTipText("Search by name…");
        tfSearch.putClientProperty("JTextField.placeholderText", "Search by name…");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        JButton btnAdd = UITheme.accentButton("+ Add Customer");
        btnAdd.addActionListener(e -> showAddDialog());
        JButton btnRefresh = UITheme.secondaryButton("↻");
        btnRefresh.addActionListener(e -> refresh());

        right.add(new JLabel("🔍"));
        right.add(tfSearch);
        right.add(btnAdd);
        right.add(btnRefresh);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────
        String[] cols = {"User ID", "Name", "Username", "Email", "Phone"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        JScrollPane sp = UITheme.styledTable(table);
        add(sp, BorderLayout.CENTER);

        // ── Action bar ────────────────────────────────────────────────
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton btnEdit   = UITheme.secondaryButton("✏️  Edit");
        JButton btnDelete = UITheme.dangerButton("🗑  Delete");
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> doDelete());
        actions.add(btnEdit);
        actions.add(btnDelete);
        add(actions, BorderLayout.SOUTH);

        refresh();
    }

    private void filterTable() {
        String text = tfSearch.getText().trim();
        if (text.isEmpty()) { sorter.setRowFilter(null); return; }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1)); // column 1 = Name
    }

    private void refresh() {
        tableModel.setRowCount(0);
        // Cache the list of customers
        customers = new java.util.ArrayList<>(UserService.getAllCustomers());
        for (User user : customers) {
            tableModel.addRow(new Object[]{user.getUserId(), user.getName(), user.getUsername(), user.getEmail(), user.getPhone()});
        }
    }

    private void showAddDialog() {
        JPanel form = buildCustomerForm(null);
        int res = JOptionPane.showConfirmDialog(this, form, "Add New Customer",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        JTextField tfUsername = (JTextField) ((JPanel)form.getComponent(0)).getComponent(1);
        JTextField tfName = (JTextField) ((JPanel)form.getComponent(1)).getComponent(1);
        JTextField tfEmail = (JTextField) ((JPanel)form.getComponent(2)).getComponent(1);
        JTextField tfPhone = (JTextField) ((JPanel)form.getComponent(3)).getComponent(1);
        JPasswordField pfPassword = (JPasswordField) ((JPanel)form.getComponent(4)).getComponent(1);

        String username = tfUsername.getText().trim(), name = tfName.getText().trim(),
               email = tfEmail.getText().trim(), phone = tfPhone.getText().trim(),
               pw = new String(pfPassword.getPassword());

        if (username.isEmpty() || name.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, Name and Password are required.");
            return;
        }
        try {
            UserService.registerUser(username, pw, name, email, phone, "Customer");
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a customer to edit."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        User user = customers.get(modelRow);

        JPanel form = buildCustomerForm(user);
        int res = JOptionPane.showConfirmDialog(this, form, "Edit Customer: " + user.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        JTextField tfName = (JTextField) ((JPanel)form.getComponent(1)).getComponent(1);
        JTextField tfEmail = (JTextField) ((JPanel)form.getComponent(2)).getComponent(1);
        JTextField tfPhone = (JTextField) ((JPanel)form.getComponent(3)).getComponent(1);

        try {
            user.setName(tfName.getText().trim());
            user.setEmail(tfEmail.getText().trim());
            user.setPhone(tfPhone.getText().trim());
            UserService.updateUser(user);
            refresh();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a customer to delete."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        User user = customers.get(modelRow);
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete customer \"" + user.getName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            UserService.deleteUser(user);
            refresh();
        }
    }

    private JPanel buildCustomerForm(User prefill) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_CARD);

        JTextField tfUsername = UITheme.styledTextField(20);
        if (prefill != null) {
            tfUsername.setText(prefill.getUsername());
            tfUsername.setEditable(false);
        }
        JTextField tfName = UITheme.styledTextField(20);
        if (prefill != null) {
            tfName.setText(prefill.getName());
        }
        JTextField tfEmail = UITheme.styledTextField(20);
        if (prefill != null) {
            tfEmail.setText(prefill.getEmail());
        }
        JTextField tfPhone = UITheme.styledTextField(20);
        if (prefill != null) {
            tfPhone.setText(prefill.getPhone() != null ? prefill.getPhone() : "");
        }
        JPasswordField pfPassword = UITheme.styledPasswordField(20);

        panel.add(UITheme.formRow("Username *", tfUsername));
        panel.add(Box.createVerticalStrut(8));
        panel.add(UITheme.formRow("Full Name *", tfName));
        panel.add(Box.createVerticalStrut(8));
        panel.add(UITheme.formRow("Email", tfEmail));
        panel.add(Box.createVerticalStrut(8));
        panel.add(UITheme.formRow("Phone", tfPhone));
        panel.add(Box.createVerticalStrut(8));
        if (prefill == null) {
            panel.add(UITheme.formRow("Password *", pfPassword));
        }
        return panel;
    }
}
