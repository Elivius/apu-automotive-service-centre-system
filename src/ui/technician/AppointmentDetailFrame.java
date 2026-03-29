package ui.technician;

import models.Appointment;
import models.Technician;
import services.AppointmentService;
import services.FeedbackService;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Popup frame showing full appointment details for a Technician.
 * Allows: reading customer comments, marking as Completed, and writing feedback.
 */
public class AppointmentDetailFrame extends JFrame {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
        setSize(600, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) { 
                if (onClose != null) {
                    onClose.run(); 
                }
            }
        });
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // ── Info ─────────────────────────────────────────────────────
        JLabel title = UITheme.titleLabel("Appointment: " + appointment.getAppointmentId());
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title); 
        card.add(Box.createVerticalStrut(16));

        card.add(infoRow("Customer ID",  appointment.getCustomerId()));
        card.add(infoRow("Service Type", appointment.getServiceType()));
        card.add(infoRow("Start Time",   appointment.getDateTime()    != null ? appointment.getDateTime().format(FMT)    : "—"));
        card.add(infoRow("End Time",     appointment.getEndDateTime() != null ? appointment.getEndDateTime().format(FMT) : "—"));

        lblStatus = UITheme.headerLabel("Status: " + appointment.getStatus());
        lblStatus.setForeground(UITheme.WARNING);
        lblStatus.setAlignmentX(LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(8)); 
        card.add(lblStatus);

        // ── Customer Comments ────────────────────────────────────────
        card.add(Box.createVerticalStrut(20));
        JLabel commLabel = UITheme.headerLabel("Customer Comments:");
        commLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(commLabel); 
        card.add(Box.createVerticalStrut(6));
        JTextArea taComments = new JTextArea(3, 40);
        taComments.setText(appointment.getComments() != null ? appointment.getComments() : "(none)");
        taComments.setEditable(false);
        JScrollPane commScroll = UITheme.styledTextArea(taComments);
        commScroll.setAlignmentX(LEFT_ALIGNMENT);
        card.add(commScroll);

        // ── Feedback ─────────────────────────────────────────────────
        card.add(Box.createVerticalStrut(20));
        JLabel fbLabel = UITheme.headerLabel("Technician Feedback:");
        fbLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(fbLabel); 
        card.add(Box.createVerticalStrut(6));
        taFeedback = new JTextArea(4, 40);
        taFeedback.setText(appointment.getFeedback() != null ? appointment.getFeedback() : "");
        JScrollPane fbScroll = UITheme.styledTextArea(taFeedback);
        fbScroll.setAlignmentX(LEFT_ALIGNMENT);
        card.add(fbScroll);

        // Disable editing if already completed
        boolean isCompleted = "Completed".equals(appointment.getStatus());
        taFeedback.setEditable(!isCompleted);

        // ── Buttons ──────────────────────────────────────────────────
        card.add(Box.createVerticalStrut(16));
        lblMsg = new JLabel(" ");
        lblMsg.setFont(UITheme.FONT_SMALL); 
        lblMsg.setForeground(UITheme.SUCCESS);
        lblMsg.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblMsg);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);

        if (!isCompleted) {
            JButton btnSaveFb = UITheme.secondaryButton("💾  Save Feedback");
            btnSaveFb.addActionListener(e -> doSaveFeedback());
            btnRow.add(btnSaveFb);

            JButton btnComplete = UITheme.accentButton("✔  Mark as Completed");
            btnComplete.addActionListener(e -> doComplete());
            btnRow.add(btnComplete);
        } else {
            JLabel done = UITheme.headerLabel("✔ This appointment is Completed.");
            done.setForeground(UITheme.SUCCESS);
            btnRow.add(done);
        }
        card.add(btnRow);

        JScrollPane outer = new JScrollPane(card);
        outer.setBorder(null); 
        outer.getViewport().setBackground(UITheme.BG_DARK);
        add(outer, BorderLayout.CENTER);
    }

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label + ":"); 
        lbl.setForeground(UITheme.TEXT_MUTED); 
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setPreferredSize(new Dimension(120, 26));
        JLabel val = new JLabel(value);  
        val.setForeground(UITheme.TEXT_PRIMARY); 
        val.setFont(UITheme.FONT_BODY);
        row.add(lbl, BorderLayout.WEST); 
        row.add(val, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return row;
    }

    private void doSaveFeedback() {
        String feedback = taFeedback.getText().trim();
        if (feedback.isEmpty()) { 
            lblMsg.setText("Feedback is empty."); 
            lblMsg.setForeground(UITheme.ACCENT); 
            return; 
        }
        FeedbackService.submitTechnicianFeedback(appointment, feedback);
        lblMsg.setText("Feedback saved!"); 
        lblMsg.setForeground(UITheme.SUCCESS);
    }

    private void doComplete() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Mark appointment " + appointment.getAppointmentId() + " as Completed?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        // Save feedback first if any
        String feedback = taFeedback.getText().trim();
        if (!feedback.isEmpty()) {
            FeedbackService.submitTechnicianFeedback(appointment, feedback);
        }
        AppointmentService.completeAppointment(appointment);
        JOptionPane.showMessageDialog(this, "Appointment marked as Completed.");
        dispose();
    }
}
