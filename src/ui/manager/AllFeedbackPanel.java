package ui.manager;

import models.Appointment;
import services.FeedbackService;
import utils.StringUtils;
import ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for Manager to view all feedback, comments, and service reviews.
 */
public class AllFeedbackPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextArea         taDetail;
    private List<Appointment> appointments;

    public AllFeedbackPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.titleLabel("All Feedback & Reviews"), BorderLayout.WEST);

        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnRefresh.addActionListener(e -> refresh());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Customer ID", "Date", "Customer Comment", "Tech Feedback", "Service Review"};
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

        // Widen columns
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);

        JScrollPane sp = UITheme.styledTable(table);
        sp.setPreferredSize(new Dimension(0, 320));

        // Detail card
        JPanel detailCard = UITheme.cardPanel();
        detailCard.setLayout(new BorderLayout(0, 8));
        detailCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        detailCard.add(UITheme.headerLabel("Selected Record — Full Details"), BorderLayout.NORTH);
        taDetail = new JTextArea(6, 60);
        taDetail.setName("taDetail");
        taDetail.setEditable(false);
        taDetail.setLineWrap(true);
        taDetail.setWrapStyleWord(true);
        detailCard.add(UITheme.styledTextArea(taDetail), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, detailCard);
        split.setDividerLocation(320);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(UITheme.BG_DARK);
        add(split, BorderLayout.CENTER);

        refresh();
    }

    void refresh() {
        tableModel.setRowCount(0);
        appointments = FeedbackService.getAllFeedback();
        for (Appointment a : appointments) {
            tableModel.addRow(new Object[]{
                a.getAppointmentId(), a.getCustomerId(),
                a.getDateTime() != null ? a.getDateTime().format(FMT) : "",
                StringUtils.truncate(a.getComments(), 40),
                StringUtils.truncate(a.getFeedback(), 40),
                StringUtils.truncate(a.getServiceReview(), 40)
            });
        }
        taDetail.setText("");
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= appointments.size()) return;
        Appointment a = appointments.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Appointment ID : ").append(a.getAppointmentId()).append("\n");
        sb.append("Customer ID    : ").append(a.getCustomerId()).append("\n");
        sb.append("Technician ID  : ").append(a.getTechnicianId()).append("\n");
        sb.append("Service Type   : ").append(a.getServiceType()).append("\n");
        sb.append("Status         : ").append(a.getStatus()).append("\n\n");
        sb.append("─── Customer Comment ───────────────────\n");
        sb.append(a.getComments() != null && !a.getComments().isEmpty() ? a.getComments() : "(none)").append("\n\n");
        sb.append("─── Technician Feedback ────────────────\n");
        sb.append(a.getFeedback() != null && !a.getFeedback().isEmpty() ? a.getFeedback() : "(none)").append("\n\n");
        sb.append("─── Customer Service Review ────────────\n");
        sb.append(a.getServiceReview() != null && !a.getServiceReview().isEmpty() ? a.getServiceReview() : "(none)");
        taDetail.setText(sb.toString());
    }
}
