package ui.technician;

import models.Appointment;
import models.Technician;
import services.AppointmentService;
import ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Shows all appointments assigned to this technician.
 * Double-click opens the AppointmentDetailPanel in a new frame.
 */
public class TechMyAppointmentsPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Technician       technician;
    private DefaultTableModel      tableModel;
    private JTable                 table;
    private List<Appointment>      appointments;

    public TechMyAppointmentsPanel(Technician technician) {
        this.technician = technician;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.titleLabel("My Assigned Appointments"), BorderLayout.WEST);
        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> refresh());
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JLabel hint = UITheme.mutedLabel("Double-click a row to view details, mark complete, or add feedback.");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] cols = {"Appt ID", "Customer ID", "Service", "Status", "Start", "End"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openDetail();
            }
        });

        JScrollPane sp = UITheme.styledTable(table);

        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setOpaque(false);
        center.add(hint, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        JButton btnOpen = UITheme.accentButton("Open Details →");
        btnOpen.addActionListener(e -> openDetail());
        bottom.add(btnOpen);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    void refresh() {
        tableModel.setRowCount(0);
        appointments = AppointmentService.getAllAppointmentsForTechnician(technician.getUserId());
        for (Appointment a : appointments) {
            tableModel.addRow(new Object[]{
                a.getAppointmentId(), a.getCustomerId(), a.getServiceType(), a.getStatus(),
                a.getDateTime()    != null ? a.getDateTime().format(FMT)    : "",
                a.getEndDateTime() != null ? a.getEndDateTime().format(FMT) : ""
            });
        }
    }

    private void openDetail() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= appointments.size()) {
            JOptionPane.showMessageDialog(this, "Please select an appointment."); return;
        }
        Appointment apt = appointments.get(row);
        AppointmentDetailFrame detail = new AppointmentDetailFrame(technician, apt, this::refresh);
        detail.setVisible(true);
    }
}
