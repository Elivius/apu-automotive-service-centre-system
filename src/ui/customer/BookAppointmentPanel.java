package ui.customer;

import models.Customer;
import services.AppointmentService;
import services.PaymentService;
import ui.UITheme;

import java.util.List;
import java.util.Date;

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

    private JComboBox<String> cbServiceType;
    private JSpinner spDate, spTime;
    private JTextArea taComments;
    private JRadioButton rbOnline, rbPhysical;
    private JLabel lblPrice, lblError, lblSuccess;

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

        // Service type
        cbServiceType = new JComboBox<>(new String[]{"Normal", "Major"});
        cbServiceType.setName("cbServiceType");
        cbServiceType.setBackground(UITheme.FIELD_BG);
        cbServiceType.setForeground(UITheme.TEXT_PRIMARY);
        cbServiceType.setFont(UITheme.FONT_BODY);
        cbServiceType.addActionListener(e -> updatePrice());

        // Price label
        lblPrice = new JLabel();
        lblPrice.setName("lblPrice");
        lblPrice.setFont(UITheme.FONT_BODY);
        lblPrice.setForeground(UITheme.SUCCESS);

        // Date spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        spDate = new JSpinner(dateModel);
        spDate.setName("spDate");

        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spDate, "yyyy-MM-dd");
        spDate.setEditor(dateEditor);
        spDate.setFont(UITheme.FONT_BODY);

        // Time spinner
        SpinnerListModel timeModel = new SpinnerListModel(buildTimeSlots());
        spTime = new JSpinner(timeModel);
        spTime.setName("spTime");
        spTime.setFont(UITheme.FONT_BODY);

        // Comments
        taComments = new JTextArea(4, 20);
        taComments.setName("taComments");
        JScrollPane commentsScroll = UITheme.styledTextArea(taComments);

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

        // Error / success labels
        lblError = new JLabel(" "); 
        lblError.setName("lblError");
        lblError.setForeground(UITheme.ACCENT);    
        lblError.setFont(UITheme.FONT_SMALL);
        lblSuccess = new JLabel(" "); 
        lblSuccess.setName("lblSuccess");
        lblSuccess.setForeground(UITheme.SUCCESS); 
        lblSuccess.setFont(UITheme.FONT_SMALL);

        JButton btnBook = UITheme.accentButton("Confirm Booking");
        btnBook.setName("btnBook");
        btnBook.addActionListener(e -> doBook());

        // Layout rows
        int row = 0;
        addFormRow(card, gbc, row++, "Service Type", cbServiceType);
        addFormRow(card, gbc, row++, "Service Price", lblPrice);
        addFormRow(card, gbc, row++, "Date", spDate);
        addFormRow(card, gbc, row++, "Time Slot", spTime);
        addFormRow(card, gbc, row++, "Comments", commentsScroll);

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
        card.add(lblError, gbc); 
        row++;
        gbc.gridy = row; 
        card.add(lblSuccess, gbc); 
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
        String type = (String) cbServiceType.getSelectedItem();
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
        lblError.setText(" ");
        lblSuccess.setText(" ");
        String serviceType = (String) cbServiceType.getSelectedItem();
        String timeSlot = (String) spTime.getValue();
        String comments = taComments.getText().trim();
        String payMethod = rbOnline.isSelected() ? "Online" : "Physical";

        // Build LocalDateTime
        Date dateVal = (Date) spDate.getValue();
        String[] timeParts = timeSlot.split(":");
        LocalDateTime dateTime = LocalDateTime.of(
            dateVal.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
            java.time.LocalTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])));

        if (dateTime.isBefore(LocalDateTime.now())) {
            lblError.setText("Please select a future date and time.");
            return;
        }

        double price = PaymentService.getServicePrice(serviceType);
        if (price <= 0) {
            lblError.setText("Service price is not set. Please contact the manager.");
            return;
        }

        try {
            AppointmentService.bookAppointment(customer.getUserId(), serviceType, dateTime, comments);
            // Retrieve the new appointment ID (last booked)
            List<models.Appointment> apts = AppointmentService.getAllAppointmentsForCustomer(customer.getUserId());
            if (!apts.isEmpty()) {
                String aptId = apts.get(apts.size() - 1).getAppointmentId();
                PaymentService.processPayment(aptId, price, payMethod);
            }
            lblSuccess.setText("Appointment booked! Status: Pending. Check 'My Appointments'.");
            taComments.setText("");
        } catch (Exception ex) {
            lblError.setText("Error: " + ex.getMessage());
        }
    }

    private String[] buildTimeSlots() {
        String[] slots = new String[28];
        int i = 0;
        for (int h = 8; h < 22; h++) {
            slots[i++] = String.format("%02d:00", h);
            slots[i++] = String.format("%02d:30", h);
        }
        return slots;
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
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(130, 28));
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(8, 0, 0, 0);
        panel.add(field, gbc);
    }
}
