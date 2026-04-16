package ui.technician;

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
        setSize(600, 600);
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
        taComments.setText(appointment.getComments() != null ? appointment.getComments() : "(none)");
        taComments.setEditable(false);
        JScrollPane commScroll = UITheme.styledTextArea(taComments);
        commScroll.setAlignmentX(LEFT_ALIGNMENT);
        commScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.add(commScroll);

        // ── Technician Feedback (editable) ───────────────────────────

        card.add(Box.createVerticalStrut(20));
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
        lblMsg.setFont(UITheme.FONT_SMALL);
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
        FeedbackService.submitTechnicianFeedback(appointment, feedback);
        lblMsg.setText("Feedback saved successfully!");
        lblMsg.setForeground(UITheme.SUCCESS);
    }

    private void doComplete() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Mark appointment " + appointment.getAppointmentId() + " as Completed?",
                "Confirm Completion", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        // Save any feedback first
        String feedback = taFeedback.getText().trim();
        if (!feedback.isEmpty()) {
            FeedbackService.submitTechnicianFeedback(appointment, feedback);
        }
        AppointmentService.completeAppointment(appointment);
        JOptionPane.showMessageDialog(this, "Appointment marked as Completed.");
        dispose();
    }
}
