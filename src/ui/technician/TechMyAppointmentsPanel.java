package ui.technician;

import models.Appointment;
import models.Technician;
import services.AppointmentService;
import utils.DateUtils;
import ui.UITheme;
import ui.PopupFieldFactory;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows all appointments assigned to this technician.
 * Double-click opens the AppointmentDetailPanel in a new frame.
 */
public class TechMyAppointmentsPanel extends JPanel {

    private final Technician technician;
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Appointment> appointments;

    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfSearch;
    private final String[] statusFilterHolder = { "All Status" };

    public TechMyAppointmentsPanel(Technician technician) {
        this.technician = technician;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(UITheme.titleLabel("My Assigned Appointments"), BorderLayout.WEST);
        
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        tfSearch = UITheme.styledTextField(14);
        tfSearch.setToolTipText("Search appointments…");
        tfSearch.putClientProperty("JTextField.placeholderText", "Search appointments…");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        String[] statusOptions = {"All Status", "Assigned", "Completed", "Declined"};
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

        JLabel hint = UITheme.mutedLabel("Double-click a row to view details, mark complete, or add feedback.");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        String[] cols = {"Appt ID", "Customer ID", "Service", "Status", "Start", "End"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openDetail();
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

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

    private void filterTable() {
        String text = tfSearch.getText().trim();
        String status = statusFilterHolder[0];
        
        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
        
        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
        
        if (status != null && !"All Status".equals(status)) {
            // column index 3 is "Status"
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(status) + "$", 3));
        }
        
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    void refresh() {
        tableModel.setRowCount(0);
        appointments = AppointmentService.getAllAppointmentsForTechnician(technician.getUserId());
        for (Appointment apt : appointments) {
            tableModel.addRow(new Object[]{
                apt.getAppointmentId(), apt.getCustomerId(), apt.getServiceType(), apt.getStatus(),
                apt.getDateTime() != null ? apt.getDateTime().format(DateUtils.FORMATTER) : "",
                apt.getEndDateTime() != null ? apt.getEndDateTime().format(DateUtils.FORMATTER) : ""
            });
        }
        if (tfSearch != null) {
            tfSearch.setText("");
        }
        statusFilterHolder[0] = "All Status";
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
    }

    private void openDetail() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= appointments.size()) {
            JOptionPane.showMessageDialog(this, "Invalid selection.");
            return;
        }
        Appointment apt = appointments.get(modelRow);
        AppointmentDetailFrame detail = new AppointmentDetailFrame(technician, apt, this::refresh);
        detail.setVisible(true);
    }
}
