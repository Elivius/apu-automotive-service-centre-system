package ui.manager;

import ui.UITheme;
import utils.AuditLogger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager-only panel that displays the full system audit log in a sortable,
 * filterable JTable. Reads from data/audit_log.txt.
 *
 * Each log entry format: TIMESTAMP:::USER_ID:::ACTION:::DETAILS
 */
public class AuditLogPanel extends JPanel {

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfSearch;

    private static final String[] COLUMNS = {"Timestamp", "User ID", "Action", "Details"};

    public AuditLogPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    private void buildUI() {
        // ── Header ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(UITheme.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));

        JLabel lblTitle = UITheme.titleLabel("Audit Log");
        lblTitle.setName("lblAuditLogTitle");
        header.add(lblTitle, BorderLayout.WEST);

        // ── Search bar + Refresh ──────────────────────────────────────────
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        tfSearch = UITheme.styledTextField(22);
        tfSearch.setName("tfAuditSearch");
        tfSearch.putClientProperty("JTextField.placeholderText", "Filter by user, action, or detail…");
        tfSearch.setMaximumSize(new Dimension(260, 32));
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnAuditRefresh");
        btnRefresh.addActionListener(e -> refresh());

        controls.add(new JLabel("Search:") {{ setForeground(UITheme.TEXT_MUTED); setFont(UITheme.FONT_BODY); }});
        controls.add(tfSearch);
        controls.add(btnRefresh);
        header.add(controls, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setName("tblAuditLog");
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        // Sorting + filtering
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Apply centralized modern theme styling
        JScrollPane scrollPane = UITheme.styledTable(table);
        scrollPane.setName("scrollAuditLog");
        // Maintain outer layout spacing while preserving theme border
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 24, 24, 24),
                scrollPane.getBorder()));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(140); // Timestamp
        table.getColumnModel().getColumn(1).setPreferredWidth(90);  // User ID
        table.getColumnModel().getColumn(2).setPreferredWidth(160); // Action
        table.getColumnModel().getColumn(3).setPreferredWidth(460); // Details

        // Colour-code rows/actions by log type with consistent theme colors for quick security scanning
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setFont(UITheme.FONT_BODY);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? UITheme.BG_CARD : UITheme.TABLE_ALT_ROW);
                    String action = (String) t.getValueAt(row, 2);
                    if (action != null) {
                        if (action.startsWith("LOGIN_FAILED") || action.startsWith("DECLINE")) {
                            setForeground(UITheme.DANGER);
                            if (col == 2) {
                                setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
                            }
                        } else if (action.startsWith("LOGIN_SUCCESS") || action.startsWith("REGISTER")) {
                            setForeground(UITheme.SUCCESS);
                            if (col == 2) {
                                setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
                            }
                        } else {
                            setForeground(UITheme.TEXT_PRIMARY);
                        }
                    } else {
                        setForeground(UITheme.TEXT_PRIMARY);
                    }
                } else {
                    String action = (String) t.getValueAt(row, 2);
                    if (action != null && (action.startsWith("LOGIN_FAILED") || action.startsWith("DECLINE") ||
                                          action.startsWith("LOGIN_SUCCESS") || action.startsWith("REGISTER"))) {
                        setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
                    }
                }
                return this;
            }
        });

        add(scrollPane, BorderLayout.CENTER);

        // ── Footer row count ──────────────────────────────────────────────
        JLabel lblCount = UITheme.mutedLabel("Showing all entries. Click a column header to sort.");
        lblCount.setName("lblAuditCount");
        lblCount.setBorder(BorderFactory.createEmptyBorder(0, 24, 12, 24));
        add(lblCount, BorderLayout.SOUTH);

        refresh();
    }

    /** Fetches the latest audit log entries from the utility layer and populates the table. */
    private void refresh() {
        tableModel.setRowCount(0);
        if (tfSearch != null) {
            tfSearch.setText("");
        }
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
        List<String[]> entries = AuditLogger.getAllLogEntriesReverse();

        for (String[] row : entries) {
            tableModel.addRow(row);
        }
    }

    /** Applies the search filter across all columns. */
    private void applyFilter() {
        String text = tfSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
    }
}
