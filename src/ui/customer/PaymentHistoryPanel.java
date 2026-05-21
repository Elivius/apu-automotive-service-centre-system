package ui.customer;

import models.Customer;
import models.Payment;
import services.PaymentService;
import utils.DateUtils;
import ui.UITheme;
import ui.PopupFieldFactory;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the customer's payment history.
 */
public class PaymentHistoryPanel extends JPanel {

    private final Customer customer;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Payment> payments;

    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfSearch;
    private final String[] statusFilterHolder = { "All Status" };
    private JLabel lblSummary;

    public PaymentHistoryPanel(Customer customer) {
        this.customer = customer;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(UITheme.titleLabel("Payment History"), BorderLayout.WEST);
        
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        tfSearch = UITheme.styledTextField(14);
        tfSearch.setToolTipText("Search payments…");
        tfSearch.putClientProperty("JTextField.placeholderText", "Search payments…");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        String[] statusOptions = {"All Status", "Pending", "Paid", "Declined"};
        JPanel statusFilterField = PopupFieldFactory.createDropdownField(statusOptions, statusFilterHolder, () -> filterTable());
        statusFilterField.setPreferredSize(new Dimension(140, 34));

        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnRefresh.addActionListener(e -> refresh());

        right.add(new JLabel("🔍") {{ setForeground(UITheme.TEXT_MUTED); }});
        right.add(tfSearch);
        right.add(statusFilterField);
        right.add(btnRefresh);

        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Payment ID", "Appointment ID", "Amount (RM)", "Method", "Status", "Payment Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setName("tablePayments");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelect();
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane sp = UITheme.styledTable(table);

        // Adjust column preferred widths for visual excellence and prevention of header/text truncation
        table.getColumnModel().getColumn(0).setPreferredWidth(110); // Payment ID
        table.getColumnModel().getColumn(1).setPreferredWidth(130); // Appointment ID
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Amount (RM)
        table.getColumnModel().getColumn(3).setPreferredWidth(110); // Method
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(5).setPreferredWidth(170); // Payment Date
        add(sp, BorderLayout.CENTER);

        // Summary label
        lblSummary = UITheme.mutedLabel("Select a row to view details.");
        lblSummary.setName("lblSummary");
        lblSummary.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        add(lblSummary, BorderLayout.SOUTH);

        refresh();
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0) {
            lblSummary.setText("Select a row to view details.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= payments.size()) {
            return;
        }
        Payment p = payments.get(modelRow);
        lblSummary.setText(String.format("Payment ID: %s | Appointment: %s | Amount: RM %.2f | Method: %s | Status: %s | Date: %s",
            p.getPaymentId(), p.getAppointmentId(), p.getAmount(), p.getPaymentMethod(), p.getPaymentStatus(),
            p.getDateTime() != null ? p.getDateTime().format(DateUtils.FORMATTER) : "N/A"));
    }

    private void filterTable() {
        String text = tfSearch.getText().trim();
        String status = statusFilterHolder[0];
        
        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
        
        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
        
        if (status != null && !"All Status".equals(status)) {
            // column index 4 is "Status"
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(status) + "$", 4));
        }
        
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    void refresh() {
        tableModel.setRowCount(0);
        payments = PaymentService.getPaymentHistory(customer.getUserId());
        for (Payment payment : payments) {
            tableModel.addRow(new Object[]{
                payment.getPaymentId(),
                payment.getAppointmentId(),
                String.format("%.2f", payment.getAmount()),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getDateTime() != null ? payment.getDateTime().format(DateUtils.FORMATTER) : ""
            });
        }
        if (lblSummary != null) {
            lblSummary.setText("Select a row to view details.");
        }
        if (tfSearch != null) {
            tfSearch.setText("");
        }
        statusFilterHolder[0] = "All Status";
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
    }
}
