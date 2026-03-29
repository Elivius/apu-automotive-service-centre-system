package ui.customer;

import models.Customer;
import models.Payment;
import services.PaymentService;
import ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Shows the customer's payment history.
 */
public class PaymentHistoryPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Customer customer;
    private DefaultTableModel tableModel;

    public PaymentHistoryPanel(Customer customer) {
        this.customer = customer;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.titleLabel("Payment History"), BorderLayout.WEST);
        
        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnRefresh.addActionListener(e -> refresh());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Payment ID", "Appointment ID", "Amount (RM)", "Method", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setName("tablePayments");

        JScrollPane sp = UITheme.styledTable(table);
        add(sp, BorderLayout.CENTER);

        // Summary label
        JLabel lblSummary = UITheme.mutedLabel("Select a row to view details.");
        lblSummary.setName("lblSummary");
        lblSummary.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        add(lblSummary, BorderLayout.SOUTH);

        refresh();
    }

    void refresh() {
        tableModel.setRowCount(0);
        List<Payment> payments = PaymentService.getPaymentHistory(customer.getUserId());
        for (Payment p : payments) {
            tableModel.addRow(new Object[]{
                p.getPaymentId(),
                p.getAppointmentId(),
                String.format("%.2f", p.getAmount()),
                p.getPaymentMethod(),
                p.getPaymentStatus(),
                p.getDateTime() != null ? p.getDateTime().format(FMT) : ""
            });
        }
    }
}
