package ui.manager;

import models.Appointment;
import services.FeedbackService;
import utils.StringUtils;
import utils.DateUtils;
import ui.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for Manager to view all feedback, comments, and service reviews.
 */
public class AllFeedbackPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea taDetail;
    private List<Appointment> appointments;

    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField tfSearch;

    public AllFeedbackPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.add(UITheme.titleLabel("All Feedback & Reviews"), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        tfSearch = UITheme.styledTextField(16);
        tfSearch.setToolTipText("Search feedback…");
        tfSearch.putClientProperty("JTextField.placeholderText", "Search feedback…");
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JButton btnAiSentiment = UITheme.aiButton("Sentiment Analysis");
        btnAiSentiment.setName("btnAiSentiment");
        btnAiSentiment.addActionListener(e -> {
            if (!services.GeminiConfig.isConfigured()) {
                JOptionPane.showMessageDialog(this, 
                    "AI service is not configured. Please set the API key in the settings first.", 
                    "Kelwin AI Sentiment Analysis Not Configured", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (appointments == null || appointments.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No feedback or reviews are currently available for sentiment analysis.", 
                    "Kelwin AI Sentiment Analysis", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            btnAiSentiment.setEnabled(false);
            btnAiSentiment.setText("✨ Analyzing...");

            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return services.GeminiService.analyzeSentiment(appointments);
                }

                @Override
                protected void done() {
                    btnAiSentiment.setEnabled(true);
                    btnAiSentiment.setText("Sentiment Analysis");
                    try {
                        String result = get();
                        showSentimentDialog(result);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AllFeedbackPanel.this,
                            "Error analyzing sentiment: " + ex.getMessage(),
                            "Kelwin AI Sentiment Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });

        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnRefresh.addActionListener(e -> refresh());

        right.add(new JLabel("🔍") {{ setForeground(UITheme.TEXT_MUTED); }});
        right.add(tfSearch);
        right.add(btnAiSentiment);
        right.add(btnRefresh);

        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Customer ID", "Appointment Date", "Customer Comment", "Tech Feedback", "Service Review"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
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

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

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
                apt.getDateTime() != null ? apt.getDateTime().format(DateUtils.FORMATTER) : "",
                StringUtils.truncate(apt.getComments(), 40),
                StringUtils.truncate(apt.getFeedback(), 40),
                StringUtils.truncate(apt.getServiceReview(), 40)
            });
        }
        taDetail.setText("");
        if (tfSearch != null) {
            tfSearch.setText("");
        }
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0) {
            taDetail.setText("");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= appointments.size()) {
            return;
        }
        Appointment apt = appointments.get(modelRow);
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

    private void filterTable() {
        String text = tfSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }
    }

    private void showSentimentDialog(String result) {
        String htmlResult = services.GeminiService.markdownToHtml(result);

        JEditorPane area = new JEditorPane();
        area.setContentType("text/html");
        area.setText(htmlResult);
        area.setEditable(false);
        area.setBackground(UITheme.FIELD_BG);
        area.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        area.setFont(UITheme.FONT_BODY);
        area.setCaretPosition(0);
        
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(680, 450));
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_CARD, 1));
        
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        JLabel title = new JLabel("✨ Kelwin AI Sentiment Analysis Report");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.SUCCESS);
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnClose = UITheme.accentButton("Dismiss");
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(btnClose);
        panel.add(footer, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Kelwin AI Sentiment Analysis", true);
        dialog.getContentPane().setBackground(UITheme.BG_DARK);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnClose.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
}
