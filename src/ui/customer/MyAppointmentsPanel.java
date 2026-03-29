package ui.customer;

import models.Appointment;
import models.Customer;
import services.AppointmentService;
import services.FeedbackService;
import services.UserService;
import ui.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Shows the customer's own appointments.
 * Allows:
 *  - Adding/editing comments on Pending/Assigned appointments.
 *  - Writing a service review on Completed appointments.
 */
public class MyAppointmentsPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Customer customer;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea taAction;
    private JButton btnSubmit;
    private JLabel lblActionTitle, lblActionHint, lblMsg;
    private List<Appointment> appointments;

    public MyAppointmentsPanel(Customer customer) {
        this.customer = customer;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        // ── Header row ────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.titleLabel("My Appointments"), BorderLayout.WEST);
        
        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnRefresh.addActionListener(e -> refresh());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────
        String[] cols = {"ID", "Service", "Date & Time", "Status", "Comments"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setName("tableAppointments");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelect();
            }
        });
        JScrollPane sp = UITheme.styledTable(table);
        sp.setPreferredSize(new Dimension(0, 320));

        // ── Detail / action panel ────────────────────────────────────
        JPanel actionCard = UITheme.cardPanel();
        actionCard.setLayout(new BorderLayout(0, 8));
        actionCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        lblActionTitle = UITheme.headerLabel("Select an appointment above");
        lblActionTitle.setName("lblActionTitle");
        lblActionHint  = UITheme.mutedLabel("");
        lblActionHint.setName("lblActionHint");
        taAction = new JTextArea(4, 30);
        taAction.setName("taAction");
        JScrollPane aScroll = UITheme.styledTextArea(taAction);

        lblMsg = new JLabel(" ");
        lblMsg.setName("lblMsg");
        lblMsg.setFont(UITheme.FONT_SMALL);
        lblMsg.setForeground(UITheme.SUCCESS);

        btnSubmit = UITheme.accentButton("Submit");
        btnSubmit.setName("btnSubmit");
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(e -> doSubmit());

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(lblActionTitle, BorderLayout.NORTH);
        top.add(lblActionHint,  BorderLayout.SOUTH);

        actionCard.add(top,     BorderLayout.NORTH);
        actionCard.add(aScroll, BorderLayout.CENTER);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setOpaque(false);
        btnRow.add(btnSubmit);
        btnRow.add(lblMsg);
        actionCard.add(btnRow,  BorderLayout.SOUTH);

        // ── Split ─────────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, actionCard);
        split.setDividerLocation(320);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(UITheme.BG_DARK);
        add(split, BorderLayout.CENTER);

        refresh();
    }

    void refresh() {
        tableModel.setRowCount(0);
        appointments = AppointmentService.getAllAppointmentsForCustomer(customer.getUserId());
        for (Appointment a : appointments) {
            tableModel.addRow(new Object[]{
                a.getAppointmentId(),
                a.getServiceType(),
                a.getDateTime() != null ? a.getDateTime().format(FMT) : "",
                a.getStatus(),
                a.getComments()
            });
        }
        lblActionTitle.setText("Select an appointment above");
        lblActionHint.setText("");
        taAction.setText("");
        taAction.setEnabled(false);
        btnSubmit.setEnabled(false);
        lblMsg.setText(" ");
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= appointments.size()) {
            return; // Prevent IndexOutOfBoundsException (User didn't select any row)
        }
        Appointment apt = appointments.get(row);
        String status = apt.getStatus();
        lblMsg.setText(" ");

        if ("Completed".equals(status)) {
            lblActionTitle.setText("Write a Service Review");
            lblActionHint.setText("Rate your experience for appointment " + apt.getAppointmentId());
            taAction.setText(apt.getServiceReview() != null ? apt.getServiceReview() : "");
            taAction.setEnabled(true);
            btnSubmit.setText("Submit Review");
            btnSubmit.setEnabled(true);
        } else if ("Pending".equals(status) || status.startsWith("Assigned")) {
            lblActionTitle.setText("Add / Edit Comments");
            lblActionHint.setText("Help the technician understand your situation");
            taAction.setText(apt.getComments() != null ? apt.getComments() : "");
            taAction.setEnabled(true);
            btnSubmit.setText("Save Comments");
            btnSubmit.setEnabled(true);
        } else {
            lblActionTitle.setText("Appointment: " + apt.getAppointmentId() + " — " + status);
            lblActionHint.setText("No actions available for this status.");
            taAction.setText("");
            taAction.setEnabled(false);
            btnSubmit.setEnabled(false);
        }
    }

    private void doSubmit() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= appointments.size()) {
            return;
        }
        Appointment apt = appointments.get(row);
        String text = taAction.getText().trim();
        if (text.isEmpty()) {
            lblMsg.setText("Please enter some text.");
            lblMsg.setForeground(UITheme.ACCENT);
            return;
        }

        if ("Completed".equals(apt.getStatus())) {
            FeedbackService.submitServiceReview(apt, text);
            lblMsg.setText("Review submitted!");
        } else {
            FeedbackService.submitCustomerComments(apt, text);
            lblMsg.setText("Comments saved!");
        } 
        
        lblMsg.setForeground(UITheme.SUCCESS);

        // Delay before refresh
        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> refresh());
        timer.setRepeats(false);
        timer.start();
    }
}
