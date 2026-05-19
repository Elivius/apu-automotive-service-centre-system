package ui.staff;

import exceptions.TechnicianUnavailableException;
import models.Appointment;
import models.CounterStaff;
import models.User;
import services.AppointmentService;
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
    private List<Appointment> appointments;
    private List<User> technicians;
    private List<User> customers;

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
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        JButton btnCreate  = UITheme.accentButton("+ New Appointment");
        btnCreate.setName("btnCreate");
        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnCreate.addActionListener(e -> showCreateDialog());
        btnRefresh.addActionListener(e -> refresh());
        right.add(new JLabel("🔍"));
        right.add(tfSearch);
        right.add(btnCreate);
        right.add(btnRefresh);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────
        String[] cols = {"Appt ID", "Customer ID", "Technician", "Service", "Status", "Date", "Time End"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        table.setName("tblAppointments");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        JScrollPane sp = UITheme.styledTable(table);
        add(sp, BorderLayout.CENTER);

        // ── Action bar ────────────────────────────────────────────────
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton btnAssign  = UITheme.accentButton("👷  Assign Technician");
        btnAssign.setName("btnAssign");
        JButton btnDecline = UITheme.dangerButton("✗  Decline");
        btnDecline.setName("btnDecline");
        btnAssign.addActionListener(e  -> showAssignDialog());
        btnDecline.addActionListener(e -> doDecline());
        actions.add(btnAssign);
        actions.add(btnDecline);
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

    void refresh() {
        tableModel.setRowCount(0);
        appointments = AppointmentService.getAllAppointments();
        for (Appointment apt : appointments) {
            tableModel.addRow(new Object[]{
                apt.getAppointmentId(), apt.getCustomerId(),
                apt.getTechnicianId().isEmpty() ? "(unassigned)" : apt.getTechnicianId(),
                apt.getServiceType(), apt.getStatus(),
                apt.getDateTime() != null ? apt.getDateTime().format(DateUtils.FORMATTER)    : "",
                apt.getEndDateTime() != null ? apt.getEndDateTime().format(DateUtils.FORMATTER) : ""
            });
        }

        // Cache reference lists
        technicians = UserService.getAllStaff().stream()
                .filter(user -> "Technician".equals(user.getRole())).collect(Collectors.toList());
        customers = new ArrayList<>(UserService.getAllCustomers());
    }

    private void showAssignDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select an appointment."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        Appointment apt = appointments.get(modelRow);

        if ("Completed".equals(apt.getStatus()) || "Declined".equals(apt.getStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot assign a " + apt.getStatus() + " appointment.");
            return;
        }

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
        } catch (TechnicianUnavailableException ex) {
            JOptionPane.showMessageDialog(this,
                "⚠ Conflict Detected!\n" + ex.getMessage(),
                "Schedule Conflict", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void doDecline() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select an appointment."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        Appointment apt = appointments.get(modelRow);

        if ("Completed".equals(apt.getStatus()) || "Declined".equals(apt.getStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot decline an appointment that is already " + apt.getStatus() + ".");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Decline appointment " + apt.getAppointmentId() + "?",
                "Confirm Decline", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            AppointmentService.declineAppointment(apt);
            refresh();
        }
    }

    private void showCreateDialog() {
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
        form.add(UITheme.formRow("Date", dateField));
        form.add(Box.createVerticalStrut(8));
        form.add(UITheme.formRow("Time", timeField));
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
