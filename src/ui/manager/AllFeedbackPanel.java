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
    private JTable table;
    private JTextArea taDetail;
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
        for (Appointment apt : appointments) {
            tableModel.addRow(new Object[]{
                apt.getAppointmentId(), apt.getCustomerId(),
                apt.getDateTime() != null ? apt.getDateTime().format(FMT) : "",
                StringUtils.truncate(apt.getComments(), 40),
                StringUtils.truncate(apt.getFeedback(), 40),
                StringUtils.truncate(apt.getServiceReview(), 40)
            });
        }
        taDetail.setText("");
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= appointments.size()) return;
        Appointment apt = appointments.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Appointment ID : ").append(apt.getAppointmentId()).append("\n");
        sb.append("Customer ID    : ").append(apt.getCustomerId()).append("\n");
        sb.append("Technician ID  : ").append(apt.getTechnicianId()).append("\n");
        sb.append("Service Type   : ").append(apt.getServiceType()).append("\n");
        sb.append("Status         : ").append(apt.getStatus()).append("\n\n");
        sb.append("─── Customer Comment ───────────────────\n");
        sb.append(apt.getComments() != null && !apt.getComments().isEmpty() ? apt.getComments() : "(none)").append("\n\n");
        sb.append("─── Technician Feedback ────────────────\n");
        sb.append(apt.getFeedback() != null && !apt.getFeedback().isEmpty() ? apt.getFeedback() : "(none)").append("\n\n");
        sb.append("─── Customer Service Review ────────────\n");
        sb.append(apt.getServiceReview() != null && !apt.getServiceReview().isEmpty() ? apt.getServiceReview() : "(none)");
        taDetail.setText(sb.toString());
    }
}
