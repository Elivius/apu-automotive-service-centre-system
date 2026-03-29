package ui.staff;

import models.Appointment;
import models.CounterStaff;
import models.Payment;
import services.AppointmentService;
import services.PaymentService;
import ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Panel for Counter Staff to collect physical payments.
 * Shows all "Pending" physical payments. Staff can confirm payment
 * and auto-generate a receipt .txt file.
 */
public class CollectPaymentPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CounterStaff staff;
    private DefaultTableModel  tableModel;
    private JTable             table;
    private List<Payment>      pendingPayments;
    private JLabel             lblReceiptPath;

    public CollectPaymentPanel(CounterStaff staff) {
        this.staff = staff;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.titleLabel("Collect Payment"), BorderLayout.WEST);

        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnRefresh.addActionListener(e -> refresh());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Hint
        JLabel hint = UITheme.mutedLabel("Showing physical payments awaiting confirmation.");
        hint.setName("lblHint");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] cols = {"Payment ID", "Appointment ID", "Amount (RM)", "Method", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        table.setName("tblPayments");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = UITheme.styledTable(table);

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

        bottom.add(btnPaid,      BorderLayout.WEST);
        bottom.add(lblReceiptPath, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    void refresh() {
        tableModel.setRowCount(0);
        // Load all payments. Keep only Physical + Pending
        List<String> lines = utils.FileHandler.getInstance().readAllLines(utils.FileHandler.PAYMENTS_FILE);
        pendingPayments = lines.stream()
                .map(Payment::fromFileString)
                .filter(payment -> payment != null && "Physical".equals(payment.getPaymentMethod()) && "Pending".equals(payment.getPaymentStatus()))
                .collect(Collectors.toList());

        for (Payment payment : pendingPayments) {
            tableModel.addRow(new Object[]{
                payment.getPaymentId(), payment.getAppointmentId(),
                String.format("%.2f", payment.getAmount()),
                payment.getPaymentMethod(), payment.getPaymentStatus(),
                payment.getDateTime() != null ? payment.getDateTime().format(FMT) : ""
            });
        }
        lblReceiptPath.setText(" ");
    }

    private void doPaidPhysically() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= pendingPayments.size()) {
            JOptionPane.showMessageDialog(this, "Please select a payment to confirm.");
            return;
        }
        Payment payment = pendingPayments.get(row);

        Appointment apt = AppointmentService.findAppointmentById(payment.getAppointmentId());
        if (apt == null) {
            JOptionPane.showMessageDialog(this, "Could not find the related appointment.");
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
