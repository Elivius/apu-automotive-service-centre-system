package ui.customer;

import models.Customer;
import services.AppointmentService;
import services.PaymentService;
import ui.PopupFieldFactory;
import ui.UITheme;

import java.util.List;
import java.time.LocalDate;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * Panel for booking a new appointment.
 * Shows live pricing, date/time spinners, service type, comments,
 * and payment method. Calls AppointmentService + PaymentService on submit.
 */
public class BookAppointmentPanel extends JPanel {

    private final Customer customer;

    private final String[] serviceTypeHolder = { "Normal" };
    private final LocalDate[] dateHolder = { LocalDate.now() };
    private final String[] timeHolder = { "08:00" };
    private JTextArea taComments;
    private JRadioButton rbOnline, rbPhysical;
    private JLabel lblPrice, lblMessage;

    public BookAppointmentPanel(Customer customer) {
        this.customer = customer;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        // ── Header ────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.titleLabel("Book Appointment"), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Form card ─────────────────────────────────────────────────
        JPanel card = UITheme.cardPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        GridBagConstraints gbc = formGBC();

        // Service type (styled dropdown)
        final JPanel serviceTypeField = PopupFieldFactory.createDropdownField(
            new String[]{"Normal", "Major"}, serviceTypeHolder, () -> updatePrice());
        serviceTypeField.setName("serviceTypeField");

        // Price label
        lblPrice = new JLabel();
        lblPrice.setName("lblPrice");
        lblPrice.setFont(UITheme.FONT_BODY);
        lblPrice.setForeground(UITheme.SUCCESS);

        // Date picker (calendar popup)
        JPanel dateField = PopupFieldFactory.createDateField(LocalDate.now(), dateHolder);
        dateField.setName("dateField");

        // Time picker (scrollable popup)
        JPanel timeField = PopupFieldFactory.createTimeField("08:00", timeHolder);
        timeField.setName("timeField");

        // Comments
        taComments = new JTextArea(4, 20);
        taComments.setName("taComments");
        JScrollPane commentsScroll = UITheme.styledTextArea(taComments);

        // Comments wrapper with AI Pre-Diagnosis button
        JPanel commentsWrapper = new JPanel(new BorderLayout(0, 6));
        commentsWrapper.setOpaque(false);
        commentsWrapper.add(commentsScroll, BorderLayout.CENTER);

        JButton btnAiDiagnose = UITheme.aiButton("Pre-Diagnosis");
        btnAiDiagnose.setName("btnAiDiagnose");
        btnAiDiagnose.addActionListener(e -> {
            String comments = taComments.getText().trim();
            if (comments.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please describe your car's symptoms in the Comments box first.", 
                    "Kelwin AI Pre-Diagnosis", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!services.GeminiConfig.isConfigured()) {
                JOptionPane.showMessageDialog(this, 
                    "AI service is not configured. Please set the API key in the settings first.", 
                    "Kelwin AI Pre-Diagnosis Not Configured", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            btnAiDiagnose.setEnabled(false);
            btnAiDiagnose.setText("✨ Analyzing...");

            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return services.GeminiService.analyzeSymptoms(comments);
                }

                @Override
                protected void done() {
                    btnAiDiagnose.setEnabled(true);
                    btnAiDiagnose.setText("✨ Kelwin AI Pre-Diagnosis");
                    try {
                        String result = get();
                        showDiagnosisDialog(result, serviceTypeField);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(BookAppointmentPanel.this,
                            "Error performing diagnosis: " + ex.getMessage(),
                            "Kelwin AI Diagnosis Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });

        JPanel btnAiPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnAiPanel.setOpaque(false);
        btnAiPanel.add(btnAiDiagnose);
        commentsWrapper.add(btnAiPanel, BorderLayout.SOUTH);

        // Payment method
        rbOnline = new JRadioButton("Online (auto-confirm)");
        rbOnline.setName("rbOnline");
        rbPhysical = new JRadioButton("Physical (pay at counter)");
        rbPhysical.setName("rbPhysical");
        rbOnline.setSelected(true);
        styleRadio(rbOnline); 
        styleRadio(rbPhysical);

        ButtonGroup bg = new ButtonGroup(); 
        bg.add(rbOnline); 
        bg.add(rbPhysical);

        // Message label
        lblMessage = new JLabel(" "); 
        lblMessage.setName("lblMessage");
        lblMessage.setFont(UITheme.FONT_BODY);

        JButton btnBook = UITheme.accentButton("Confirm Booking");
        btnBook.setName("btnBook");
        btnBook.addActionListener(e -> doBook());

        // Layout rows
        int row = 0;
        addFormRow(card, gbc, row++, "Service Type", serviceTypeField);
        addFormRow(card, gbc, row++, "Service Price", lblPrice);
        addFormRow(card, gbc, row++, "Appointment Date", dateField);
        addFormRow(card, gbc, row++, "Time Slot", timeField);
        addFormRow(card, gbc, row++, "Comments", commentsWrapper);

        JPanel pmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pmPanel.setOpaque(false);
        pmPanel.add(rbOnline); 
        pmPanel.add(Box.createHorizontalStrut(16)); 
        pmPanel.add(rbPhysical);
        addFormRow(card, gbc, row++, "Payment Method", pmPanel);

        gbc.gridx = 0; 
        gbc.gridy = row; 
        gbc.gridwidth = 2; 
        gbc.insets = new Insets(12, 0, 0, 0);
        card.add(lblMessage, gbc); 
        row++;
        gbc.gridy = row; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.WEST;
        card.add(btnBook, gbc);

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setName("scrollPane");
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK);
        add(scrollPane, BorderLayout.CENTER);

        updatePrice();
    }

    private void updatePrice() {
        String type = serviceTypeHolder[0];
        double price = PaymentService.getServicePrice(type);
        if (price <= 0) {
            lblPrice.setText("Price not set — contact manager");
            lblPrice.setForeground(UITheme.WARNING);
        } else {
            lblPrice.setText("RM " + String.format("%.2f", price));
            lblPrice.setForeground(UITheme.SUCCESS);
        }
    }

    private void doBook() {
        lblMessage.setText(" ");
        String serviceType = serviceTypeHolder[0];
        String timeSlot = timeHolder[0];
        String comments = taComments.getText().trim();
        String payMethod = rbOnline.isSelected() ? "Online" : "Physical";

        // Build LocalDateTime from calendar picker date + time slot
        String[] timeParts = timeSlot.split(":");
        LocalDateTime dateTime = LocalDateTime.of(
            dateHolder[0],
            java.time.LocalTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])));

        if (dateTime.isBefore(LocalDateTime.now())) {
            lblMessage.setForeground(UITheme.DANGER);
            lblMessage.setText("Please select a future date and time.");
            return;
        }

        if (comments.isEmpty()) {
            lblMessage.setForeground(UITheme.DANGER);
            lblMessage.setText("Please enter a comment describing your service needs.");
            return;
        }

        double price = PaymentService.getServicePrice(serviceType);
        if (price <= 0) {
            lblMessage.setForeground(UITheme.DANGER);
            lblMessage.setText("Service price is not set. Please contact the manager.");
            return;
        }

        try {
            AppointmentService.bookAppointment(customer.getUserId(), serviceType, dateTime, comments, payMethod);
            lblMessage.setForeground(UITheme.SUCCESS);
            lblMessage.setText("Appointment booked! Status: Pending. Check 'My Appointments'.");
            taComments.setText("");
        } catch (Exception ex) {
            lblMessage.setForeground(UITheme.DANGER);
            lblMessage.setText("Error: " + ex.getMessage());
        }
    }

    private void showDiagnosisDialog(String result, JPanel serviceTypeField) {
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
        
        JLabel title = new JLabel("✨ Kelwin AI Pre-Diagnosis Report");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.SUCCESS);
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnApplyNormal = UITheme.secondaryButton("Apply 'Normal' Service");
        JButton btnApplyMajor = UITheme.secondaryButton("Apply 'Major' Service");
        JButton btnClose = UITheme.accentButton("Dismiss");
        
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(btnApplyNormal);
        footer.add(btnApplyMajor);
        footer.add(btnClose);
        
        panel.add(footer, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Kelwin AI Pre-Diagnosis", true);
        dialog.getContentPane().setBackground(UITheme.BG_DARK);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnApplyNormal.addActionListener(e -> {
            serviceTypeHolder[0] = "Normal";
            try {
                if (serviceTypeField.getComponent(0) instanceof JTextField) {
                    ((JTextField) serviceTypeField.getComponent(0)).setText("Normal");
                }
            } catch (Exception ex) {}
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Service type set to Normal. Please verify pricing.");
            updatePrice();
        });

        btnApplyMajor.addActionListener(e -> {
            serviceTypeHolder[0] = "Major";
            try {
                if (serviceTypeField.getComponent(0) instanceof JTextField) {
                    ((JTextField) serviceTypeField.getComponent(0)).setText("Major");
                }
            } catch (Exception ex) {}
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Service type set to Major. Please verify pricing.");
            updatePrice();
        });

        btnClose.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }



    private void styleRadio(JRadioButton rb) {
        rb.setBackground(UITheme.BG_CARD);
        rb.setForeground(UITheme.TEXT_PRIMARY);
        rb.setFont(UITheme.FONT_BODY);
        rb.setFocusPainted(false);
    }

    private GridBagConstraints formGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.insets = new Insets(8, 0, 0, 16);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(130, 28));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 0, 0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }
}
