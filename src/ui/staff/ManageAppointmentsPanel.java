package ui.staff;

import exceptions.ConcurrencyException;
import exceptions.TechnicianUnavailableException;
import models.Appointment;
import models.CounterStaff;
import models.Payment;
import models.User;
import models.Customer;
import services.AppointmentService;
import services.PaymentService;
import services.UserService;
import utils.DateUtils;
import ui.PopupFieldFactory;
import ui.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Appointment management panel for Counter Staff.
 * - View all appointments with smart search (RowFilter).
 * - Create new appointments for customers.
 * - Assign a technician (collision-aware scheduling).
 * - Update or Decline appointments.
 */
public class ManageAppointmentsPanel extends JPanel {

    private final CounterStaff staff;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfSearch;
    private String[] statusFilterHolder = { "All Status" };
    private List<Appointment> appointments;
    private Map<String, Payment> paymentMap;

    public ManageAppointmentsPanel(CounterStaff staff) {
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
        header.add(UITheme.titleLabel("Manage Appointments"), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        tfSearch = UITheme.styledTextField(18);
        tfSearch.setToolTipText("Search by name, phone, ID…");
        tfSearch.putClientProperty("JTextField.placeholderText", "Search by name, phone, ID…");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        // Status filter dropdown (styled popup)
        String[] statusOptions = {"All Status", "Pending", "Assigned", "Completed", "Declined"};
        JPanel statusFilterField = PopupFieldFactory.createDropdownField(statusOptions, statusFilterHolder, () -> filterTable());
        statusFilterField.setPreferredSize(new Dimension(140, 34));

        JButton btnCreate  = UITheme.accentButton("+ New Appointment");
        btnCreate.setName("btnCreate");
        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnCreate.addActionListener(e -> showCreateDialog());
        btnRefresh.addActionListener(e -> refresh());
        right.add(new JLabel("🔍"));
        right.add(tfSearch);
        right.add(statusFilterField);
        right.add(btnCreate);
        right.add(btnRefresh);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────
        String[] cols = {"Appt ID", "Customer ID", "Customer Name", "Phone", "Technician", "Service", "Status", "Appointment Date", "Time End", "Payment"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        table.setName("tblAppointments");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        JScrollPane sp = UITheme.styledTable(table);
        add(sp, BorderLayout.CENTER);

        // ── Action bar ────────────────────────────────────────────────
        JButton btnAssign  = UITheme.accentButton("👷  Assign Technician");
        btnAssign.setName("btnAssign");
        JButton btnCollect = UITheme.secondaryButton("💳  Collect Payment");
        btnCollect.setName("btnCollectPayment");
        JButton btnDecline = UITheme.dangerButton("✗  Decline");
        btnDecline.setName("btnDecline");
        btnAssign.addActionListener(e  -> showAssignDialog());
        btnCollect.addActionListener(e -> doCollectPayment());
        btnDecline.addActionListener(e -> doDecline());

        JPanel actionsPanel = new JPanel(new BorderLayout());
        actionsPanel.setOpaque(false);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);
        leftActions.add(btnAssign);
        leftActions.add(btnCollect);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);
        rightActions.add(btnDecline);

        actionsPanel.add(leftActions, BorderLayout.WEST);
        actionsPanel.add(rightActions, BorderLayout.EAST);
        add(actionsPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void filterTable() {
        String text = tfSearch.getText().trim();
        String status = statusFilterHolder[0];

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        // Text search filter (searches across ALL columns including name/phone)
        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }

        // Status dropdown filter (column index 6 = "Status")
        if (status != null && !"All Status".equals(status)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(status) + "$", 6));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    void refresh() {
        tableModel.setRowCount(0);
        appointments = AppointmentService.getAllAppointments();

        // Build a lookup map: customerId -> User for fast name/phone resolution
        Map<String, Customer> customerMap = UserService.getAllCustomersMap();

        // Build a lookup map: appointmentId -> Payment for payment status resolution
        paymentMap = PaymentService.getAllPaymentsMapByAppointment();

        for (Appointment apt : appointments) {
            User cust = customerMap.get(apt.getCustomerId());
            String custName = cust != null ? cust.getName() : "";
            String custPhone = cust != null && cust.getPhone() != null ? cust.getPhone() : "";

            Payment payment = paymentMap.get(apt.getAppointmentId());
            String payStatus = payment != null ? payment.getPaymentStatus() : "No Payment";

            tableModel.addRow(new Object[]{
                apt.getAppointmentId(), apt.getCustomerId(),
                custName, custPhone,
                apt.getTechnicianId().isEmpty() ? "(unassigned)" : apt.getTechnicianId(),
                apt.getServiceType(), apt.getStatus(),
                apt.getDateTime() != null ? apt.getDateTime().format(DateUtils.FORMATTER)    : "",
                apt.getEndDateTime() != null ? apt.getEndDateTime().format(DateUtils.FORMATTER) : "",
                payStatus
            });
        }
    }

    private void showAssignDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select an appointment."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= appointments.size()) {
            JOptionPane.showMessageDialog(this, "Please select a valid appointment.");
            return;
        }
        Appointment apt = appointments.get(modelRow);

        if ("Completed".equals(apt.getStatus()) || "Declined".equals(apt.getStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot assign a " + apt.getStatus() + " appointment.");
            return;
        }

        List<User> technicians = new ArrayList<>(UserService.getAllTechnicians());
        if (technicians.isEmpty()) { JOptionPane.showMessageDialog(this, "No technicians registered."); return; }

        String[] techNames = technicians.stream()
                .map(tech -> tech.getUserId() + " — " + tech.getName()).toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(this,
                "Select a technician for appointment " + apt.getAppointmentId() + ":",
                "Assign Technician", JOptionPane.PLAIN_MESSAGE, null, techNames, techNames[0]);
        if (chosen == null) return;

        String techId = chosen.split(" — ")[0].trim();
        try {
            AppointmentService.assignAppointment(apt, techId);
            JOptionPane.showMessageDialog(this, "Assigned to " + techId + " successfully.");
            refresh();

            // After assign: check if payment is pending → offer to collect
            Payment pendingPayment = paymentMap.get(apt.getAppointmentId());
            if (pendingPayment != null && "Pending".equals(pendingPayment.getPaymentStatus())) {
                int payOk = JOptionPane.showConfirmDialog(this,
                    "This appointment has a pending physical payment of RM "
                    + String.format("%.2f", pendingPayment.getAmount())
                    + ".\nWould you like to collect payment now?",
                    "Pending Payment Found", JOptionPane.YES_NO_OPTION);
                if (payOk == JOptionPane.YES_OPTION) {
                    PaymentService.confirmPhysicalPayment(pendingPayment);
                    String receiptPath = PaymentService.generateReceipt(pendingPayment, apt);
                    JOptionPane.showMessageDialog(this,
                        "Payment confirmed!\nReceipt generated at:\n" + receiptPath,
                        "Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    refresh();
                }
            }
        } catch (TechnicianUnavailableException ex) {
            JOptionPane.showMessageDialog(this,
                "Error: Conflict Detected!\n" + ex.getMessage(),
                "Schedule Conflict", JOptionPane.WARNING_MESSAGE);
        } catch (ConcurrencyException ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Concurrency Error", JOptionPane.ERROR_MESSAGE);
            refresh();
        }
    }

    private void doDecline() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return; 
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= appointments.size()) {
            JOptionPane.showMessageDialog(this, "Please select a valid appointment.");
            return;
        }
        Appointment apt = appointments.get(modelRow);

        if ("Completed".equals(apt.getStatus()) || "Declined".equals(apt.getStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot decline an appointment that is already " + apt.getStatus() + ".");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Decline appointment " + apt.getAppointmentId() + "? This cannot be undone.",
                "Confirm Decline", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                AppointmentService.declineAppointment(apt);
                JOptionPane.showMessageDialog(this, "Appointment declined successfully.");
                refresh();
            } catch (ConcurrencyException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "Concurrency Error", JOptionPane.ERROR_MESSAGE);
                refresh();
            }
        }
    }

    private void doCollectPayment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= appointments.size()) {
            JOptionPane.showMessageDialog(this, "Please select a valid appointment.");
            return;
        }
        Appointment apt = appointments.get(modelRow);

        // Check if technician is assigned first
        if (apt.getTechnicianId() == null || apt.getTechnicianId().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot collect payment — no technician assigned yet.\nPlease assign a technician first.",
                "No Technician Assigned", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Find the pending physical payment for this appointment
        Payment pendingPayment = paymentMap.get(apt.getAppointmentId());

        if (pendingPayment == null || !"Pending".equals(pendingPayment.getPaymentStatus()) || !"Physical".equals(pendingPayment.getPaymentMethod())) {
            JOptionPane.showMessageDialog(this, "No pending physical payment found for this appointment.\nThe payment may already be confirmed or was made online.",
                "No Pending Payment", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Confirm physical payment of RM " + String.format("%.2f", pendingPayment.getAmount())
                + " for appointment " + apt.getAppointmentId() + "?",
                "Confirm Payment", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        PaymentService.confirmPhysicalPayment(pendingPayment);
        String receiptPath = PaymentService.generateReceipt(pendingPayment, apt);

        JOptionPane.showMessageDialog(this,
                "Payment confirmed!\nReceipt generated at:\n" + receiptPath,
                "Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);
        refresh();
    }

    private void showCreateDialog() {
        List<User> customers = new ArrayList<>(UserService.getAllCustomers());
        if (customers.isEmpty()) { JOptionPane.showMessageDialog(this, "No customers found."); return; }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.BG_CARD);

        String[] custNames = customers.stream()
                .map(customer -> customer.getUserId() + " — " + customer.getName()).toArray(String[]::new);
        String[] customerHolder = { custNames[0] };
        JPanel customerField = PopupFieldFactory.createDropdownField(custNames, customerHolder, null);

        String[] serviceHolder = { "Normal" };
        JPanel serviceField = PopupFieldFactory.createDropdownField(
            new String[]{"Normal", "Major"}, serviceHolder, null);

        // Calendar date picker
        LocalDate[] dateHolder = { LocalDate.now() };
        JPanel dateField = PopupFieldFactory.createDateField(LocalDate.now(), dateHolder);

        // Time picker (scrollable popup)
        String[] timeHolder = { "08:00" };
        JPanel timeField = PopupFieldFactory.createTimeField("08:00", timeHolder);

        JTextArea taComments = new JTextArea(3, 20);
        taComments.setBackground(UITheme.FIELD_BG);
        taComments.setForeground(UITheme.TEXT_PRIMARY);

        form.add(UITheme.formRow("Customer", customerField));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Service Type", serviceField));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Appointment Date", dateField));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Time Slot", timeField));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Comments", new JScrollPane(taComments)));

        int res = JOptionPane.showConfirmDialog(this, form, "Create Appointment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String custId = customerHolder[0].split(" — ")[0].trim();
        String serviceType = serviceHolder[0];

        // Combine date + time slot into LocalDateTime
        String timeSlot = timeHolder[0];
        String[] timeParts = timeSlot.split(":");
        LocalDateTime dateTime = LocalDateTime.of(
            dateHolder[0],
            LocalTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])));

        String comments = taComments.getText().trim();

        if (comments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Comments are required. Please describe the service needs.");
            return;
        }
        if (dateTime.isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this, "Please select a future date and time.");
            return;
        }

        AppointmentService.bookAppointment(custId, serviceType, dateTime, comments, "Physical");
        refresh();
        JOptionPane.showMessageDialog(this, "Appointment created successfully.");
    }
}
