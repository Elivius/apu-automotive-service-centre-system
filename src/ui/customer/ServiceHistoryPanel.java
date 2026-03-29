package ui.customer;

import models.Appointment;
import models.Customer;
import services.FeedbackService;
import utils.StringUtils;
import ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Shows the customer's service history — appointments that
 * have technician feedback written on them.
 */
public class ServiceHistoryPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Customer customer;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea taDetail;
    private List<Appointment> appointments;

    public ServiceHistoryPanel(Customer customer) {
        this.customer = customer;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.titleLabel("Service History"), BorderLayout.WEST);

        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnRefresh.addActionListener(e -> refresh());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Service", "Date & Time", "Status", "Technician Feedback"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setName("tableAppointments");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSelect();
        });

        JScrollPane sp = UITheme.styledTable(table);
        sp.setPreferredSize(new Dimension(0, 300));

        // Detail panel
        JPanel detailCard = UITheme.cardPanel();
        detailCard.setLayout(new BorderLayout(0, 8));
        detailCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        detailCard.add(UITheme.headerLabel("Technician Feedback Detail"), BorderLayout.NORTH);

        taDetail = new JTextArea(6, 40);
        taDetail.setName("taDetail");
        taDetail.setEditable(false);
        taDetail.setLineWrap(true);
        taDetail.setWrapStyleWord(true);
        detailCard.add(UITheme.styledTextArea(taDetail), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, detailCard);
        split.setDividerLocation(300);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(UITheme.BG_DARK);
        add(split, BorderLayout.CENTER);

        refresh();
    }

    void refresh() {
        tableModel.setRowCount(0);
        appointments = FeedbackService.getFeedbackForCustomer(customer.getUserId());
        for (Appointment apt : appointments) {
            String feedback = apt.getFeedback();
            // Show only first 40 characters of feedback
            String preview = StringUtils.truncate(feedback, 40);
            tableModel.addRow(new Object[]{
                apt.getAppointmentId(),
                apt.getServiceType(),
                apt.getDateTime() != null ? apt.getDateTime().format(FMT) : "",
                apt.getStatus(),
                preview
            });
        }
        taDetail.setText("");
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= appointments.size()) return;
        Appointment apt = appointments.get(row);
        taDetail.setText(apt.getFeedback() != null ? apt.getFeedback() : "(no feedback)");
    }
}
