package ui.manager;

import services.PaymentService;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Panel for Manager to view and set service prices.
 */
public class ServicePricesPanel extends JPanel {

    private JSpinner spNormal, spMajor;
    private JLabel lblMsg;

    public ServicePricesPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new GridBagLayout()); // Use GridBag to center the card
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        // ── Title & Intro ────────────────────────────────────────────
        JLabel title = UITheme.titleLabel("Set Service Prices");
        title.setName("lblTitle");
        title.setAlignmentX(CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(16));

        JLabel hint = UITheme.mutedLabel("Prices are in Malaysian Ringgit (RM) and apply to new appointments.");
        hint.setName("lblHint");
        hint.setAlignmentX(CENTER_ALIGNMENT);
        card.add(hint);
        card.add(Box.createVerticalStrut(32));

        // ── Pricing Inputs ───────────────────────────────────────────
        double normalPrice = PaymentService.getServicePrice("Normal");
        double majorPrice  = PaymentService.getServicePrice("Major");

        SpinnerNumberModel normalModel = new SpinnerNumberModel(
                normalPrice > 0 ? normalPrice : 100.0, 0.0, 99999.0, 10.0);
        SpinnerNumberModel majorModel  = new SpinnerNumberModel(
                majorPrice  > 0 ? majorPrice  : 300.0, 0.0, 99999.0, 10.0);

        spNormal = new JSpinner(normalModel);
        spNormal.setName("spNormal");
        spMajor  = new JSpinner(majorModel);
        spMajor.setName("spMajor");
        styleSpinner(spNormal);
        styleSpinner(spMajor);

        card.add(pricingRow("🔧  Normal Service", "Duration: 1 hour", spNormal));
        card.add(Box.createVerticalStrut(24));
        card.add(pricingRow("🛠  Major Service",  "Duration: 3 hours", spMajor));
        card.add(Box.createVerticalStrut(40));

        // ── Status & Footer ──────────────────────────────────────────
        lblMsg = new JLabel(" ");
        lblMsg.setName("lblMsg");
        lblMsg.setFont(UITheme.FONT_BODY);
        lblMsg.setAlignmentX(CENTER_ALIGNMENT);
        card.add(lblMsg);
        card.add(Box.createVerticalStrut(16));

        JButton btnSave = UITheme.accentButton("💾  Save All Prices");
        btnSave.setName("btnSave");
        btnSave.setAlignmentX(CENTER_ALIGNMENT);
        btnSave.addActionListener(e -> doSave());
        card.add(btnSave);

        // GridBag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 40, 0);
        add(card, gbc);
    }

    private JPanel pricingRow(String title, String subtitle, JSpinner spinner) {
        JPanel row = new JPanel(new BorderLayout(32, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(650, 80));

        // Left side: Text info
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = UITheme.headerLabel(title);
        lblTitle.setFont(UITheme.FONT_TITLE.deriveFont(18f));
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(UITheme.mutedLabel(subtitle));
        
        row.add(left, BorderLayout.CENTER);

        // Right side
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        right.setOpaque(false);
        spinner.setPreferredSize(new Dimension(180, 44));
        right.add(spinner);

        row.add(right, BorderLayout.EAST);
        return row;
    }

    private void styleSpinner(JSpinner sp) {
        sp.setBackground(UITheme.FIELD_BG);
        sp.setForeground(UITheme.TEXT_PRIMARY);
        sp.setFont(UITheme.FONT_BODY);
        
        // Style the text field
        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(UITheme.FIELD_BG);
            tf.setForeground(UITheme.TEXT_PRIMARY);
            tf.setCaretColor(UITheme.ACCENT);
            tf.setFont(UITheme.FONT_BODY);
            tf.setBorder(null); // Remove inner border
        }
        
        // Modern borders and padding
        sp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.FIELD_BORDER, 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));
    }

    private void doSave() {
        double normalPrice = (Double) spNormal.getValue();
        double majorPrice = (Double) spMajor.getValue();
        
        if (normalPrice <= 0 || majorPrice <= 0) {
            lblMsg.setText("Prices must be greater than zero.");
            lblMsg.setForeground(UITheme.DANGER);
            return;
        }
        
        PaymentService.setServicePrice("Normal", normalPrice);
        PaymentService.setServicePrice("Major",  majorPrice);
        
        lblMsg.setText("✔ Prices updated successfully!");
        lblMsg.setForeground(UITheme.SUCCESS);
        
        // Clear message after 3 seconds
        Timer timer = new Timer(3000, e -> lblMsg.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }
}
