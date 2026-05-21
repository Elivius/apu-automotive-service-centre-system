package ui.technician;

import exceptions.ConcurrencyException;
import models.Appointment;
import models.Technician;
import services.AppointmentService;
import services.FeedbackService;
import utils.DateUtils;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Popup frame showing full appointment details for a Technician.
 * Allows: reading customer comments, marking as Completed, and writing feedback.
 */
public class AppointmentDetailFrame extends JFrame {

    private final Technician technician;
    private final Appointment appointment;
    private final Runnable onClose;

    private JTextArea taFeedback;
    private JLabel lblStatus, lblMsg;

    public AppointmentDetailFrame(Technician technician, Appointment appointment, Runnable onClose) {
        this.technician = technician;
        this.appointment = appointment;
        this.onClose = onClose;
        setTitle("Appointment Details — " + appointment.getAppointmentId());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(850, 800);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(UITheme.BG_DARK);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                if (onClose != null) onClose.run();
            }
        });
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // ── Title ────────────────────────────────────────────────────
        JLabel title = UITheme.titleLabel("Appointment: " + appointment.getAppointmentId());
        title.setAlignmentX(LEFT_ALIGNMENT);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(title);
        card.add(Box.createVerticalStrut(16));

        // ── Info rows ────────────────────────────────────────────────
        card.add(infoRow("Customer ID",  appointment.getCustomerId()));
        card.add(Box.createVerticalStrut(4));
        card.add(infoRow("Service Type", appointment.getServiceType()));
        card.add(Box.createVerticalStrut(4));
        card.add(infoRow("Start Time",   appointment.getDateTime() != null
                ? appointment.getDateTime().format(DateUtils.FORMATTER) : "—"));
        card.add(Box.createVerticalStrut(4));
        card.add(infoRow("End Time",     appointment.getEndDateTime() != null
                ? appointment.getEndDateTime().format(DateUtils.FORMATTER) : "—"));

        // ── Status ───────────────────────────────────────────────────
        boolean isCompleted = Appointment.STATUS_COMPLETED.equals(appointment.getStatus());
        boolean isDeclined  = Appointment.STATUS_DECLINED.equals(appointment.getStatus());

        lblStatus = UITheme.headerLabel("Status: " + appointment.getStatus());
        
        if (isCompleted) {
            lblStatus.setForeground(UITheme.SUCCESS);
        } else if (isDeclined) {
            lblStatus.setForeground(UITheme.DANGER);
        } else {
            lblStatus.setForeground(UITheme.WARNING);
        }

        lblStatus.setAlignmentX(LEFT_ALIGNMENT);
        lblStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        card.add(Box.createVerticalStrut(10));
        card.add(lblStatus);

        // ── Divider ──────────────────────────────────────────────────
        JSeparator divider = UITheme.sectionDivider();
        divider.setAlignmentX(LEFT_ALIGNMENT);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        card.add(Box.createVerticalStrut(16));
        card.add(divider);
        card.add(Box.createVerticalStrut(16));

        // ── Customer Comments (read-only) ────────────────────────────
        JLabel commLabel = UITheme.headerLabel("Customer Comments");
        commLabel.setAlignmentX(LEFT_ALIGNMENT);
        commLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        card.add(commLabel);
        card.add(Box.createVerticalStrut(8));

        JTextArea taComments = new JTextArea(3, 40);
        taComments.setText(appointment.getComments() != null && !appointment.getComments().trim().isEmpty() ? appointment.getComments() : "(none)");
        taComments.setEditable(false);
        JScrollPane commScroll = UITheme.styledTextArea(taComments);
        commScroll.setAlignmentX(LEFT_ALIGNMENT);
        commScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.add(commScroll);

        // ✨ AI Diagnostic Guide
        JButton btnAiGuide = UITheme.aiButton("Diagnostic Guide");
        btnAiGuide.setName("btnAiGuide");
        btnAiGuide.setAlignmentX(LEFT_ALIGNMENT);
        btnAiGuide.addActionListener(e -> {
            if (!services.GeminiConfig.isConfigured()) {
                JOptionPane.showMessageDialog(this, 
                    "AI service is not configured. Please set the API key in the settings first.", 
                    "Kelwin AI Diagnostic Guide Not Configured", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            btnAiGuide.setEnabled(false);
            btnAiGuide.setText("✨ Generating...");
            
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override protected String doInBackground() throws Exception {
                    return services.GeminiService.generateDiagnosticChecklist(
                        appointment.getServiceType(), appointment.getComments());
                }
                @Override protected void done() {
                    btnAiGuide.setEnabled(true);
                    btnAiGuide.setText("Diagnostic Guide");
                    try {
                        String result = get();
                        showChecklistDialog(result);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AppointmentDetailFrame.this,
                            "Error generating checklist: " + ex.getMessage(),
                            "Kelwin AI Diagnostic Guide Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });

        JPanel guideWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        guideWrapper.setOpaque(false);
        guideWrapper.setAlignmentX(LEFT_ALIGNMENT);
        guideWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        guideWrapper.add(btnAiGuide);
        card.add(Box.createVerticalStrut(6));
        card.add(guideWrapper);

        // ── Technician Feedback (editable) ───────────────────────────

        card.add(Box.createVerticalStrut(10));
        JLabel fbLabel = UITheme.headerLabel("Technician Feedback"
                + (isCompleted || isDeclined ? "" : " (editable)"));
        fbLabel.setAlignmentX(LEFT_ALIGNMENT);
        fbLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        card.add(fbLabel);
        card.add(Box.createVerticalStrut(8));

        taFeedback = new JTextArea(5, 40);
        taFeedback.setText(appointment.getFeedback() != null ? appointment.getFeedback() : "");
        taFeedback.setEditable(!isCompleted && !isDeclined);
        JScrollPane fbScroll = UITheme.styledTextArea(taFeedback);
        fbScroll.setAlignmentX(LEFT_ALIGNMENT);
        fbScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.add(fbScroll);

        // ── Status message ───────────────────────────────────────────
        card.add(Box.createVerticalStrut(14));
        lblMsg = new JLabel(" ");
        lblMsg.setFont(UITheme.FONT_BODY);
        lblMsg.setForeground(UITheme.SUCCESS);
        lblMsg.setAlignmentX(LEFT_ALIGNMENT);
        lblMsg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        card.add(lblMsg);
        card.add(Box.createVerticalStrut(8));

        // ── Action buttons ─────────────────────────────────────────── 
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        if (!isCompleted && !isDeclined) {
            JButton btnSaveFb = UITheme.secondaryButton("💾  Save Feedback");
            btnSaveFb.addActionListener(e -> doSaveFeedback());
            btnRow.add(btnSaveFb);

            JButton btnPolish = UITheme.aiButton("Polish Feedback");
            btnPolish.setName("btnPolish");
            btnPolish.addActionListener(e -> {
                String rawFeedback = taFeedback.getText().trim();
                if (rawFeedback.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Please write some raw feedback in the text area first.", 
                        "Kelwin AI Polish Feedback", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!services.GeminiConfig.isConfigured()) {
                    JOptionPane.showMessageDialog(this, 
                        "AI service is not configured. Please set the API key in the settings first.", 
                        "Kelwin AI Polish Feedback Not Configured", 
                        JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                btnPolish.setEnabled(false);
                btnPolish.setText("✨ Polishing...");
                
                SwingWorker<String, Void> worker = new SwingWorker<>() {
                    @Override protected String doInBackground() throws Exception {
                        return services.GeminiService.polishFeedback(rawFeedback);
                    }
                    @Override protected void done() {
                        btnPolish.setEnabled(true);
                        btnPolish.setText("Polish Feedback");
                        try {
                            String result = get();
                            showPolishConfirmationDialog(rawFeedback, result);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(AppointmentDetailFrame.this,
                                "Error polishing feedback: " + ex.getMessage(),
                                "Kelwin AI Polish Feedback Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            });
            btnRow.add(btnPolish);

            JButton btnComplete = UITheme.accentButton("✔  Mark as Completed");
            btnComplete.addActionListener(e -> doComplete());
            btnRow.add(btnComplete);
        } else if (isDeclined) {
            JLabel done = UITheme.headerLabel("❌  This appointment is Declined.");
            done.setForeground(UITheme.DANGER);
            btnRow.add(done);
        } else if (isCompleted) {
            JLabel done = UITheme.headerLabel("✔  This appointment is Completed.");
            done.setForeground(UITheme.SUCCESS);
            btnRow.add(done);
        }
        card.add(btnRow);

        // ── Outer scroll pane ────────────────────────────────────────
        // Wrap in a padded panel so the card has breathing room
        JPanel padded = new JPanel(new GridBagLayout());
        padded.setBackground(UITheme.BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(20, 20, 20, 20);
        padded.add(card, gbc);

        JScrollPane outer = new JScrollPane(padded);
        outer.setBorder(null);
        outer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        outer.getVerticalScrollBar().setUnitIncrement(16);
        outer.getViewport().setBackground(UITheme.BG_DARK);
        add(outer, BorderLayout.CENTER);
    }

    /**
     * A two-column info row: muted label on the left (fixed 120px),
     * value text on the right filling remaining space.
     * Uses LEFT_ALIGNMENT so it plays nicely with card's BoxLayout Y_AXIS.
     */
    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lbl = new JLabel(label + ":");
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setPreferredSize(new Dimension(120, 26));

        JLabel val = new JLabel(value);
        val.setForeground(UITheme.TEXT_PRIMARY);
        val.setFont(UITheme.FONT_BODY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    private void doSaveFeedback() {
        String feedback = taFeedback.getText().trim();
        if (feedback.isEmpty()) {
            lblMsg.setText("Feedback cannot be empty.");
            lblMsg.setForeground(UITheme.DANGER);
            return;
        }
        try {
            FeedbackService.submitTechnicianFeedback(appointment, feedback);
            lblMsg.setText("Feedback saved successfully!");
            lblMsg.setForeground(UITheme.SUCCESS);
        } catch (ConcurrencyException ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Concurrency Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doComplete() {
        String feedback = taFeedback.getText().trim();
        if (feedback.isEmpty()) {
            lblMsg.setText("Please enter feedback before marking as completed.");
            lblMsg.setForeground(UITheme.DANGER);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Mark appointment " + appointment.getAppointmentId() + " as Completed?",
                "Confirm Completion", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            FeedbackService.submitTechnicianFeedback(appointment, feedback);
            AppointmentService.completeAppointment(appointment);
            JOptionPane.showMessageDialog(this, "Appointment marked as Completed.");
            dispose();
        } catch (ConcurrencyException ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Concurrency Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showChecklistDialog(String result) {
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
        
        JLabel title = new JLabel("✨ Kelwin AI Diagnostic & Service Checklist");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.SUCCESS);
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnClose = UITheme.accentButton("Dismiss");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(btnClose);
        panel.add(footer, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "Kelwin AI Diagnostic Guide", true);
        dialog.getContentPane().setBackground(UITheme.BG_DARK);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnClose.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showPolishConfirmationDialog(String before, String after) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(750, 320));

        JTextArea taBefore = new JTextArea(before);
        taBefore.setEditable(false);
        taBefore.setLineWrap(true);
        taBefore.setWrapStyleWord(true);
        taBefore.setBackground(UITheme.FIELD_BG);
        taBefore.setForeground(UITheme.TEXT_MUTED);
        JScrollPane spBefore = new JScrollPane(taBefore);
        spBefore.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_CARD, 1), 
            "Original Feedback", 0, 0, UITheme.FONT_SMALL, UITheme.TEXT_MUTED));
        spBefore.setOpaque(false);
        spBefore.getViewport().setOpaque(false);

        JEditorPane taAfter = new JEditorPane();
        taAfter.setContentType("text/html");
        taAfter.setText(services.GeminiService.markdownToHtml(after));
        taAfter.setEditable(false);
        taAfter.setBackground(UITheme.FIELD_BG);
        taAfter.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        taAfter.setFont(UITheme.FONT_BODY);
        JScrollPane spAfter = new JScrollPane(taAfter);
        spAfter.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.SUCCESS, 1), 
            "✨ Polished Feedback", 0, 0, UITheme.FONT_SMALL, UITheme.SUCCESS));
        spAfter.setOpaque(false);
        spAfter.getViewport().setOpaque(false);

        panel.add(spBefore);
        panel.add(spAfter);

        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        JLabel title = new JLabel("Would you like to replace your original feedback with the polished version?");
        title.setFont(UITheme.FONT_BODY);
        title.setForeground(UITheme.TEXT_PRIMARY);
        
        outer.add(title, BorderLayout.NORTH);
        outer.add(panel, BorderLayout.CENTER);

        JButton btnAccept = UITheme.accentButton("Accept Polished Feedback");
        JButton btnReject = UITheme.secondaryButton("Keep Original");
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(btnReject);
        footer.add(btnAccept);
        outer.add(footer, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "Kelwin AI Feedback Polisher", true);
        dialog.getContentPane().setBackground(UITheme.BG_DARK);
        dialog.getContentPane().add(outer);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnAccept.addActionListener(e -> {
            taFeedback.setText(after);
            dialog.dispose();
        });
        btnReject.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}
