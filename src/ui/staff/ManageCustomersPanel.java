package ui.staff;

import models.Appointment;
import models.CounterStaff;
import models.Payment;
import models.User;
import exceptions.ConcurrencyException;
import exceptions.TechnicianUnavailableException;
import services.AppointmentService;
import services.PaymentService;
import services.UserService;
import ui.UITheme;
import utils.DateUtils;
import utils.InputValidator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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
        // ── Header ───────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        header.add(UITheme.titleLabel("Manage Customers"), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        tfSearch = UITheme.styledTextField(18);
        tfSearch.setToolTipText("Search by name, phone, email…");
        tfSearch.putClientProperty("JTextField.placeholderText", "Search by name, phone, email…");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        right.add(new JLabel("🔍"));
        right.add(tfSearch);

        JButton btnAdd = UITheme.accentButton("+ Add");
        btnAdd.addActionListener(e -> showCustomerForm(null));

        JButton btnEdit = UITheme.warningButton("✏️  Edit");
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a customer to edit.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow < 0 || modelRow >= customers.size()) {
                JOptionPane.showMessageDialog(this, "Please select a valid customer.");
                return;
            }
            User user = customers.get(modelRow);
            showCustomerForm(user);
        });

        JButton btnDelete = UITheme.dangerButton("🗑  Delete");
        btnDelete.addActionListener(e -> doDelete());

        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> refresh());

        JButton btnViewAppts = UITheme.accentButton("📅  View Appointments");
        btnViewAppts.setName("btnViewAppointments");
        btnViewAppts.addActionListener(e -> doViewAppointments());

        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);
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
        // Double-click a customer row → open View Appointments dialog
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    doViewAppointments();
                }
            }
        });
        JScrollPane sp = UITheme.styledTable(table);
        add(sp, BorderLayout.CENTER);

        // ── Bottom Action Bar ─────────────────────────────────────────
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnViewAppts);
        add(actions, BorderLayout.SOUTH);

        refresh();
    }

    private void filterTable() {
        String text = tfSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
    }

    private void refresh() {
        tableModel.setRowCount(0);
        // Cache the list of customers
        customers = new java.util.ArrayList<>(UserService.getAllCustomers());
        for (User user : customers) {
            tableModel.addRow(new Object[]{user.getUserId(), user.getName(), user.getUsername(), user.getEmail(), user.getPhone()});
        }

        if (tfSearch != null) {
            tfSearch.setText("");
        }
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
    }

    private void showCustomerForm(User prefillUser) {
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
        JPasswordField pfPassword = UITheme.styledPasswordField(20);
        pfPassword.setName("pfPassword");
        JPasswordField pfConfirmPassword = UITheme.styledPasswordField(20);
        pfConfirmPassword.setName("pfConfirmPassword");

        if (prefillUser != null) {
            tfUsername.setText(prefillUser.getUsername());
            tfUsername.setEditable(false);
            tfName.setText(prefillUser.getName());
            tfEmail.setText(prefillUser.getEmail());
            tfPhone.setText(prefillUser.getPhone() != null ? prefillUser.getPhone() : "");
        }

        form.add(UITheme.formRow("Username *", tfUsername));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Full Name *", tfName));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Email *", tfEmail));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Phone", tfPhone));
        form.add(Box.createVerticalStrut(8));
        if (prefillUser == null) {
            form.add(UITheme.formRow("Password *", pfPassword));
            form.add(Box.createVerticalStrut(8));
            form.add(UITheme.formRow("Confirm Password *", pfConfirmPassword));
            form.add(Box.createVerticalStrut(8));
        }

        String title = prefillUser == null ? "Add New Customer" : "Edit Customer: " + prefillUser.getName();
        int res = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        if (prefillUser == null) {
            String username = tfUsername.getText().trim();
            String name = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            String phone = tfPhone.getText().trim();
            String password = new String(pfPassword.getPassword());
            String confirmPassword = new String(pfConfirmPassword.getPassword());

            if (username.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username, Name, Email and Password are required.");
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
                UserService.registerUser(username, password, name, email, phone, "Customer");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        } else {
            String name = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            String phone = tfPhone.getText().trim();
            
            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Email are required.");
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
                UserService.updateUserProfile(prefillUser, name, email, phone, null);
                refresh();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a customer to delete."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= customers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a valid customer.");
            return;
        }
        User user = customers.get(modelRow);
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete customer \"" + user.getName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            UserService.deleteUser(user);
            refresh();
        }
    }

    private void doViewAppointments() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a customer first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= customers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a valid customer.");
            return;
        }
        User customer = customers.get(modelRow);

        // ── Build a proper JDialog instead of JOptionPane ──────────────
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Customer Appointments — " + customer.getName(), true);
        dialog.setSize(1100, 650);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BG_CARD);
        dialog.setLayout(new BorderLayout(0, 8));

        // ── Header info ───────────────────────────────────────────────
        JLabel lblInfo = UITheme.headerLabel("Appointments for " + customer.getName() + " (" + customer.getUserId() + ")");
        lblInfo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        JLabel lblPhone = UITheme.mutedLabel("Phone: " + (customer.getPhone() != null && !customer.getPhone().isEmpty() ? customer.getPhone() : "N/A"));
        lblPhone.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblPhone.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(lblInfo);
        infoPanel.add(lblPhone);
        dialog.add(infoPanel, BorderLayout.NORTH);

        // ── Appointment table ─────────────────────────────────────────
        String[] cols = {"Appt ID", "Service", "Status", "Technician", "Appointment Date", "Customer Comment", "Payment"};
        DefaultTableModel dtm = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable apptTable = new JTable(dtm);
        apptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = UITheme.styledTable(apptTable);

        // Increase row height and font sizes for better readability
        apptTable.setRowHeight(40);
        apptTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        apptTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));

        // Widen columns to prevent truncation and make it spacious
        apptTable.getColumnModel().getColumn(0).setPreferredWidth(110); // Appt ID
        apptTable.getColumnModel().getColumn(1).setPreferredWidth(130); // Service
        apptTable.getColumnModel().getColumn(2).setPreferredWidth(110); // Status
        apptTable.getColumnModel().getColumn(3).setPreferredWidth(140); // Technician
        apptTable.getColumnModel().getColumn(4).setPreferredWidth(190); // Appointment Date
        apptTable.getColumnModel().getColumn(5).setPreferredWidth(180); // Comment
        apptTable.getColumnModel().getColumn(6).setPreferredWidth(130); // Payment

        // Helper: loads/refreshes the appointment data into the dialog table
        // We use a final array so the lambda can reference it
        final List<Appointment>[] custApptsRef = new List[]{null};
        final Map<String, Payment>[] paymentMapRef = new Map[]{null};

        Runnable refreshDialogTable = () -> {
            dtm.setRowCount(0);
            custApptsRef[0] = AppointmentService.getAllAppointmentsForCustomer(customer.getUserId());
            List<Payment> custPayments = PaymentService.getPaymentHistory(customer.getUserId());

            paymentMapRef[0] = new HashMap<>();
            for (Payment p : custPayments) {
                paymentMapRef[0].put(p.getAppointmentId(), p);
            }

            for (Appointment apt : custApptsRef[0]) {
                Payment pay = paymentMapRef[0].get(apt.getAppointmentId());
                String payStatus = pay != null ? pay.getPaymentStatus() : "No Payment";
                dtm.addRow(new Object[]{
                    apt.getAppointmentId(),
                    apt.getServiceType(),
                    apt.getStatus(),
                    apt.getTechnicianId().isEmpty() ? "(unassigned)" : apt.getTechnicianId(),
                    apt.getDateTime() != null ? apt.getDateTime().format(DateUtils.FORMATTER) : "",
                    apt.getComments() != null ? apt.getComments() : "",
                    payStatus
                });
            }
        };
        refreshDialogTable.run();

        dialog.add(sp, BorderLayout.CENTER);

        // ── Action buttons ────────────────────────────────────────────
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JButton btnAssign  = UITheme.accentButton("\uD83D\uDC77  Assign Technician");
        JButton btnCollect = UITheme.secondaryButton("\uD83D\uDCB3  Collect Payment");
        JButton btnDecline = UITheme.dangerButton("\u2717  Decline");

        // ── Assign Technician action ──────────────────────────────────
        btnAssign.addActionListener(e -> {
            int selRow = apptTable.getSelectedRow();
            if (selRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select an appointment.");
                return;
            }
            Appointment apt = custApptsRef[0].get(selRow);

            if ("Completed".equals(apt.getStatus()) || "Declined".equals(apt.getStatus())) {
                JOptionPane.showMessageDialog(dialog, "Cannot assign a " + apt.getStatus() + " appointment.");
                return;
            }

            List<User> technicians = new ArrayList<>(UserService.getAllTechnicians());
            if (technicians.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No technicians registered.");
                return;
            }

            String[] techNames = technicians.stream()
                    .map(tech -> tech.getUserId() + " — " + tech.getName()).toArray(String[]::new);
            String chosen = (String) JOptionPane.showInputDialog(dialog,
                    "Select a technician for appointment " + apt.getAppointmentId() + ":",
                    "Assign Technician", JOptionPane.PLAIN_MESSAGE, null, techNames, techNames[0]);
            if (chosen == null) return;

            String techId = chosen.split(" — ")[0].trim();
            try {
                AppointmentService.assignAppointment(apt, techId);
                JOptionPane.showMessageDialog(dialog, "Assigned to " + techId + " successfully.");
                refreshDialogTable.run();

                // ── After assign: check if payment is pending → offer to collect ──
                Payment pendingPay = paymentMapRef[0].get(apt.getAppointmentId());
                if (pendingPay != null && "Pending".equals(pendingPay.getPaymentStatus())) {
                    int payOk = JOptionPane.showConfirmDialog(dialog,
                        "This appointment has a pending physical payment of RM "
                        + String.format("%.2f", pendingPay.getAmount())
                        + ".\nWould you like to collect payment now?",
                        "Pending Payment Found", JOptionPane.YES_NO_OPTION);
                    if (payOk == JOptionPane.YES_OPTION) {
                        // Re-fetch the appointment to get the latest version after assign
                        Appointment freshApt = AppointmentService.findAppointmentById(apt.getAppointmentId());
                        PaymentService.confirmPhysicalPayment(pendingPay);
                        String receiptPath = PaymentService.generateReceipt(pendingPay, freshApt != null ? freshApt : apt);
                        JOptionPane.showMessageDialog(dialog,
                            "Payment confirmed!\nReceipt generated at:\n" + receiptPath,
                            "Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);
                        refreshDialogTable.run();
                    }
                }
            } catch (TechnicianUnavailableException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error: Conflict Detected!\n" + ex.getMessage(),
                    "Schedule Conflict", JOptionPane.WARNING_MESSAGE);
            } catch (ConcurrencyException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error: " + ex.getMessage(),
                    "Concurrency Error", JOptionPane.ERROR_MESSAGE);
                refreshDialogTable.run();
            }
        });

        // ── Collect Payment action ────────────────────────────────────
        btnCollect.addActionListener(e -> {
            int selRow = apptTable.getSelectedRow();
            if (selRow < 0) { JOptionPane.showMessageDialog(dialog, "Please select an appointment."); return; }
            Appointment apt = custApptsRef[0].get(selRow);

            if (apt.getTechnicianId() == null || apt.getTechnicianId().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Cannot collect payment — no technician assigned yet.\nPlease assign a technician first.",
                    "No Technician Assigned", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Payment pendingPay = paymentMapRef[0].get(apt.getAppointmentId());
            if (pendingPay == null || !"Pending".equals(pendingPay.getPaymentStatus())) {
                JOptionPane.showMessageDialog(dialog,
                    "No pending physical payment for this appointment.\nPayment may already be confirmed or was made online.",
                    "No Pending Payment", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int payOk = JOptionPane.showConfirmDialog(dialog,
                "Confirm physical payment of RM " + String.format("%.2f", pendingPay.getAmount())
                + " for appointment " + apt.getAppointmentId() + "?",
                "Confirm Payment", JOptionPane.YES_NO_OPTION);
            if (payOk != JOptionPane.YES_OPTION) return;

            PaymentService.confirmPhysicalPayment(pendingPay);
            String receiptPath = PaymentService.generateReceipt(pendingPay, apt);
            JOptionPane.showMessageDialog(dialog,
                "Payment confirmed!\nReceipt generated at:\n" + receiptPath,
                "Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);
            refreshDialogTable.run();
        });

        // ── Decline action ────────────────────────────────────────────
        btnDecline.addActionListener(e -> {
            int selRow = apptTable.getSelectedRow();
            if (selRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select an appointment.");
                return;
            }
            Appointment apt = custApptsRef[0].get(selRow);

            if ("Completed".equals(apt.getStatus()) || "Declined".equals(apt.getStatus())) {
                JOptionPane.showMessageDialog(dialog, "Cannot decline an appointment that is already " + apt.getStatus() + ".");
                return;
            }

            int ok = JOptionPane.showConfirmDialog(dialog,
                    "Decline appointment " + apt.getAppointmentId() + "? This cannot be undone.",
                    "Confirm Decline", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                try {
                    AppointmentService.declineAppointment(apt);
                    JOptionPane.showMessageDialog(dialog, "Appointment declined successfully.");
                    refreshDialogTable.run();
                } catch (ConcurrencyException ex) {
                    JOptionPane.showMessageDialog(dialog,
                        "Error: " + ex.getMessage(),
                        "Concurrency Error", JOptionPane.ERROR_MESSAGE);
                    refreshDialogTable.run();
                }
            }
        });

        JPanel actionsPanel = new JPanel(new BorderLayout());
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);
        leftActions.add(btnAssign);
        leftActions.add(btnCollect);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);
        rightActions.add(btnDecline);

        actionsPanel.add(leftActions, BorderLayout.WEST);
        actionsPanel.add(rightActions, BorderLayout.EAST);
        dialog.add(actionsPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}