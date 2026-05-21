package ui.customer;

import exceptions.ConcurrencyException;
import models.Appointment;
import models.Customer;
import services.AppointmentService;
import services.FeedbackService;
import utils.DateUtils;
import ui.UITheme;
import ui.PopupFieldFactory;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the customer's own appointments and service history in a unified hub.
 * Allows:
 *  - Checking active appointments (Pending/Assigned) and updating comments.
 *  - Reading technician feedback & diagnosis for completed appointments.
 *  - Submitting/editing service reviews on completed appointments.
 */
public class MyAppointmentsPanel extends JPanel {

    private final Customer customer;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Appointment> appointments;
    
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfSearch;
    private final String[] statusFilterHolder = { "All Status" };

    // UI elements for detail card
    private JLabel lblActionTitle, lblActionHint, lblMsg, lblEmpty;
    private JButton btnSubmit;
    private JPanel detailCardContainer;
    private CardLayout detailCardLayout;
    
    // Components inside card container
    private JTextArea taComments;  // Active comments area
    private JTextArea taFeedback;  // Completed read-only technician diagnostics
    private JTextArea taReview;    // Completed editable customer service review

    public MyAppointmentsPanel(Customer customer) {
        this.customer = customer;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        // ── Header row ────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(UITheme.titleLabel("My Appointments"), BorderLayout.WEST);
        
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

        String[] statusOptions = {"All Status", "Pending", "Assigned", "Completed", "Declined"};
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

        // ── Table ─────────────────────────────────────────────────────
        String[] cols = {"Appt ID", "Service", "Appointment Date & Time", "Technician", "Status", "Comments"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setName("tableAppointments");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelect();
            }
        });
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        JScrollPane sp = UITheme.styledTable(table);
        sp.setPreferredSize(new Dimension(0, 240));

        // ── Detail / action panel ────────────────────────────────────
        JPanel actionCard = UITheme.cardPanel();
        actionCard.setLayout(new BorderLayout(0, 12));
        actionCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        lblActionTitle = UITheme.headerLabel("Select an appointment above");
        lblActionTitle.setName("lblActionTitle");
        lblActionHint  = UITheme.mutedLabel("");
        lblActionHint.setFont(UITheme.FONT_BODY);
        lblActionHint.setName("lblActionHint");

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(lblActionTitle, BorderLayout.NORTH);
        top.add(lblActionHint,  BorderLayout.SOUTH);
        actionCard.add(top,     BorderLayout.NORTH);

        // Card Container for dynamic status-specific views
        detailCardLayout = new CardLayout();
        detailCardContainer = new JPanel(detailCardLayout);
        detailCardContainer.setOpaque(false);

        // 1. Empty/Declined Panel
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setOpaque(false);
        lblEmpty = UITheme.mutedLabel("Select an appointment to view details, feedback, or leave comments.");
        lblEmpty.setFont(UITheme.FONT_BODY);
        lblEmpty.setName("lblEmpty");
        emptyPanel.add(lblEmpty);

        // 2. Active Panel (Pending/Assigned)
        JPanel activePanel = new JPanel(new BorderLayout(0, 8));
        activePanel.setOpaque(false);
        JLabel lblCommentsHeader = UITheme.mutedLabel("Provide Comments for your Technician:");
        lblCommentsHeader.setFont(UITheme.FONT_BODY);
        lblCommentsHeader.setIcon(UITheme.commentsIcon(UITheme.TEXT_MUTED));
        lblCommentsHeader.setIconTextGap(8);
        activePanel.add(lblCommentsHeader, BorderLayout.NORTH);
        taComments = new JTextArea(4, 30);
        taComments.setName("taComments");
        taComments.setLineWrap(true);
        taComments.setWrapStyleWord(true);
        activePanel.add(UITheme.styledTextArea(taComments), BorderLayout.CENTER);

        // 3. Completed Panel (Diagnostics + Review Split)
        JPanel completedPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        completedPanel.setOpaque(false);

        // Left: Feedback (Read-Only)
        JPanel feedbackPanel = new JPanel(new BorderLayout(0, 6));
        feedbackPanel.setOpaque(false);
        JLabel lblFeedbackHeader = UITheme.mutedLabel("Technician Diagnosis & Feedback");
        lblFeedbackHeader.setFont(UITheme.FONT_BODY);
        lblFeedbackHeader.setIcon(UITheme.diagnosticsIcon(UITheme.TEXT_MUTED));
        lblFeedbackHeader.setIconTextGap(8);
        feedbackPanel.add(lblFeedbackHeader, BorderLayout.NORTH);
        taFeedback = new JTextArea(4, 20);
        taFeedback.setName("taFeedback");
        taFeedback.setEditable(false);
        taFeedback.setLineWrap(true);
        taFeedback.setWrapStyleWord(true);
        feedbackPanel.add(UITheme.styledTextArea(taFeedback), BorderLayout.CENTER);

        // Right: Review (Editable)
        JPanel reviewPanel = new JPanel(new BorderLayout(0, 6));
        reviewPanel.setOpaque(false);
        JLabel lblReviewHeader = UITheme.mutedLabel("My Service Review");
        lblReviewHeader.setFont(UITheme.FONT_BODY);
        lblReviewHeader.setIcon(UITheme.reviewIcon(UITheme.TEXT_MUTED));
        lblReviewHeader.setIconTextGap(8);
        reviewPanel.add(lblReviewHeader, BorderLayout.NORTH);
        taReview = new JTextArea(4, 20);
        taReview.setName("taReview");
        taReview.setLineWrap(true);
        taReview.setWrapStyleWord(true);
        reviewPanel.add(UITheme.styledTextArea(taReview), BorderLayout.CENTER);

        completedPanel.add(feedbackPanel);
        completedPanel.add(reviewPanel);

        detailCardContainer.add(emptyPanel, "empty");
        detailCardContainer.add(activePanel, "active");
        detailCardContainer.add(completedPanel, "completed");

        actionCard.add(detailCardContainer, BorderLayout.CENTER);

        // Button Row at bottom
        lblMsg = new JLabel(" ");
        lblMsg.setName("lblMsg");
        lblMsg.setFont(UITheme.FONT_BODY);
        lblMsg.setForeground(UITheme.SUCCESS);

        btnSubmit = UITheme.accentButton("Save Comments");
        btnSubmit.setName("btnSubmit");
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(e -> doSubmit());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnSubmit);
        btnRow.add(lblMsg);
        actionCard.add(btnRow, BorderLayout.SOUTH);

        // ── Split Pane ────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, actionCard);
        split.setDividerLocation(260);
        split.setResizeWeight(0.5);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(UITheme.BG_DARK);
        add(split, BorderLayout.CENTER);

        refresh();
    }

    void refresh() {
        tableModel.setRowCount(0);
        appointments = AppointmentService.getAllAppointmentsForCustomer(customer.getUserId());
        for (Appointment apt : appointments) {
            tableModel.addRow(new Object[]{
                apt.getAppointmentId(),
                apt.getServiceType(),
                apt.getDateTime() != null ? apt.getDateTime().format(DateUtils.FORMATTER) : "",
                apt.getTechnicianId() != null && !apt.getTechnicianId().isEmpty() ? apt.getTechnicianId() : "-",
                apt.getStatus(),
                apt.getComments()
            });
        }
        lblActionTitle.setText("Select an appointment above");
        lblActionHint.setText("");
        lblEmpty.setText("Select an appointment to view details, feedback, or leave comments.");
        detailCardLayout.show(detailCardContainer, "empty");
        btnSubmit.setEnabled(false);
        lblMsg.setText(" ");
        
        // Reset sorting and filtering inputs
        if (tfSearch != null) {
            tfSearch.setText("");
        }
        statusFilterHolder[0] = "All Status";
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0) {
            detailCardLayout.show(detailCardContainer, "empty");
            lblActionTitle.setText("Select an appointment above");
            lblActionHint.setText("");
            lblEmpty.setText("Select an appointment to view details, feedback, or leave comments.");
            btnSubmit.setEnabled(false);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= appointments.size()) {
            return;
        }
        Appointment apt = appointments.get(modelRow);
        String status = apt.getStatus();
        lblMsg.setText(" ");

        if ("Completed".equals(status)) {
            lblActionTitle.setText("Service Completed & Feedback Available");
            lblActionHint.setText("Review your technician's diagnostics and rate your experience below.");
            taFeedback.setText(apt.getFeedback() != null && !apt.getFeedback().isEmpty() ? apt.getFeedback() : "(No diagnostics written by technician yet)");
            taReview.setText(apt.getServiceReview() != null ? apt.getServiceReview() : "");
            
            detailCardLayout.show(detailCardContainer, "completed");
            btnSubmit.setText("Submit Review");
            btnSubmit.setEnabled(true);
        } else if ("Pending".equals(status) || "Assigned".equals(status)) {
            lblActionTitle.setText("Add / Edit Appointment Comments");
            lblActionHint.setText("Help the technician prepare for your vehicle's service.");
            taComments.setText(apt.getComments() != null ? apt.getComments() : "");
            
            detailCardLayout.show(detailCardContainer, "active");
            btnSubmit.setText("Save Comments");
            btnSubmit.setEnabled(true);
        } else if ("Declined".equals(status)) {
            lblActionTitle.setText("Appointment Declined");
            lblActionHint.setText("This appointment was declined by the service center.");
            lblEmpty.setText("No actions available for declined appointments.");
            
            detailCardLayout.show(detailCardContainer, "empty");
            btnSubmit.setEnabled(false);
        } else {
            lblActionTitle.setText("Appointment ID: " + apt.getAppointmentId());
            lblActionHint.setText("Status: " + status);
            lblEmpty.setText("No actions available for " + status + " status.");
            
            detailCardLayout.show(detailCardContainer, "empty");
            btnSubmit.setEnabled(false);
        }
    }

    private void doSubmit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= appointments.size()) {
            return;
        }
        Appointment apt = appointments.get(modelRow);
        String text;
        if ("Completed".equals(apt.getStatus())) {
            text = taReview.getText().trim();
        } else {
            text = taComments.getText().trim();
        }

        if (text.isEmpty()) {
            if ("Completed".equals(apt.getStatus())) {
                lblMsg.setText("Please enter review text.");
            } else {
                lblMsg.setText("Please enter comment text.");
            }
            lblMsg.setForeground(UITheme.DANGER);
            return;
        }

        try {
            if ("Completed".equals(apt.getStatus())) {
                FeedbackService.submitServiceReview(apt, text);
                lblMsg.setText("Review submitted successfully!");
            } else {
                FeedbackService.submitCustomerComments(apt, text);
                lblMsg.setText("Comments saved successfully!");
            } 
            lblMsg.setForeground(UITheme.SUCCESS);
        } catch (ConcurrencyException ex) {
            lblMsg.setText("Error: " + ex.getMessage());
            lblMsg.setForeground(UITheme.DANGER);
        }

        // pep refresh (1.5 seconds)
        javax.swing.Timer timer = new javax.swing.Timer(1500, e -> refresh());
        timer.setRepeats(false);
        timer.start();
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
}
