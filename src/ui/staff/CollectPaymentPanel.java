package ui.staff;

import models.Appointment;
import models.CounterStaff;
import models.Payment;
import models.User;
import models.Customer;
import services.AppointmentService;
import services.PaymentService;
import services.UserService;
import utils.DateUtils;
import ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Panel for Counter Staff to collect physical payments.
 * Shows all "Pending" physical payments. Staff can confirm payment
 * and auto-generate a receipt .txt file.
 */
public class CollectPaymentPanel extends JPanel {
    private final CounterStaff staff;
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfSearch;
    private List<Payment> pendingPayments;
    private JLabel lblReceiptPath;

    public CollectPaymentPanel(CounterStaff staff) {
        this.staff = staff;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(UITheme.titleLabel("Collect Payment"), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        tfSearch = UITheme.styledTextField(18);
        tfSearch.setToolTipText("Search by name, phone, appointment ID…");
        tfSearch.putClientProperty("JTextField.placeholderText", "Search by name, phone, ID…");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnRefresh.addActionListener(e -> refresh());
        right.add(new JLabel("🔍"));
        right.add(tfSearch);
        right.add(btnRefresh);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Hint
        JLabel hint = UITheme.mutedLabel("Showing physical payments awaiting confirmation. Search by customer name or phone.");
        hint.setName("lblHint");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] cols = {"Payment ID", "Appointment ID", "Customer Name", "Phone", "Service", "Amount (RM)", "Method", "Status", "Payment Date"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        table.setName("tblPayments");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        JScrollPane sp = UITheme.styledTable(table);

        // Adjust column preferred widths for visual excellence and prevention of header/text truncation
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Payment ID
        table.getColumnModel().getColumn(1).setPreferredWidth(110); // Appointment ID
        table.getColumnModel().getColumn(2).setPreferredWidth(140); // Customer Name
        table.getColumnModel().getColumn(3).setPreferredWidth(110); // Phone
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Service
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Amount (RM)
        table.getColumnModel().getColumn(6).setPreferredWidth(95);  // Method
        table.getColumnModel().getColumn(7).setPreferredWidth(90);  // Status
        table.getColumnModel().getColumn(8).setPreferredWidth(160); // Payment Date

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(hint, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Bottom action bar
        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton btnPaid = UITheme.accentButton("✔  Paid Physically — Confirm & Generate Receipt");
        btnPaid.setName("btnPaid");
        btnPaid.addActionListener(e -> doPaidPhysically());

        lblReceiptPath = UITheme.mutedLabel(" ");
        lblReceiptPath.setName("lblReceiptPath");
        lblReceiptPath.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        bottom.add(btnPaid, BorderLayout.WEST);
        bottom.add(lblReceiptPath, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

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
        pendingPayments = PaymentService.getPendingPhysicalPayments();

        // Build customer lookup map for name/phone resolution
        Map<String, Customer> customerMap = UserService.getAllCustomersMap();

        // Build appointment lookup map for service type resolution
        Map<String, Appointment> aptMap = AppointmentService.getAllAppointmentsMap();

        for (Payment payment : pendingPayments) {
            Appointment apt = aptMap.get(payment.getAppointmentId());
            String custName = "";
            String custPhone = "";
            String serviceType = "";
            if (apt != null) {
                User cust = customerMap.get(apt.getCustomerId());
                custName = cust != null ? cust.getName() : "";
                custPhone = cust != null && cust.getPhone() != null ? cust.getPhone() : "";
                serviceType = apt.getServiceType() != null ? apt.getServiceType() : "";
            }

            tableModel.addRow(new Object[]{
                payment.getPaymentId(), payment.getAppointmentId(),
                custName, custPhone, serviceType,
                String.format("%.2f", payment.getAmount()),
                payment.getPaymentMethod(), payment.getPaymentStatus(),
                payment.getDateTime() != null ? payment.getDateTime().format(DateUtils.FORMATTER) : ""
            });
        }
        lblReceiptPath.setText(" ");
    }

    private void doPaidPhysically() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a payment to confirm.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= pendingPayments.size()) {
            JOptionPane.showMessageDialog(this, "Please select a valid payment.");
            return;
        }
        Payment payment = pendingPayments.get(modelRow);

        Appointment apt = AppointmentService.findAppointmentById(payment.getAppointmentId());
        if (apt == null) {
            JOptionPane.showMessageDialog(this, "Could not find the related appointment.");
            return;
        }
        
        if (apt.getTechnicianId() == null || apt.getTechnicianId().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Cannot collect payment — no technician assigned yet.\nPlease assign a technician first.",
                "No Technician Assigned", JOptionPane.WARNING_MESSAGE);
            return;
		}

        int ok = JOptionPane.showConfirmDialog(this,
                "Confirm physical payment of RM " + String.format("%.2f", payment.getAmount())
                + " for appointment " + apt.getAppointmentId() + "?",
                "Confirm Payment", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        PaymentService.confirmPhysicalPayment(payment);
        String receiptPath = PaymentService.generateReceipt(payment, apt);
        lblReceiptPath.setText("Receipt saved: " + receiptPath);
        lblReceiptPath.setForeground(UITheme.SUCCESS);

        JOptionPane.showMessageDialog(this,
                "Payment confirmed!\nReceipt generated at:\n" + receiptPath,
                "Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);
        refresh();
    }
}
