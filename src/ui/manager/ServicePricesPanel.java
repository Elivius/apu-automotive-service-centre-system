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
    private JLabel   lblMsg;

    public ServicePricesPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

        JLabel title = UITheme.titleLabel("Set Service Prices");
        title.setName("lblTitle");
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title); card.add(Box.createVerticalStrut(8));

        JLabel hint = UITheme.mutedLabel("Prices are in Malaysian Ringgit (RM) and apply to new appointments immediately.");
        hint.setName("lblHint");
        hint.setAlignmentX(LEFT_ALIGNMENT);
        card.add(hint); card.add(Box.createVerticalStrut(28));

        // Load current prices
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

        card.add(UITheme.formRow("Normal Service (RM)", spNormal));
        card.add(Box.createVerticalStrut(4));
        card.add(UITheme.mutedLabel("  Duration: 1 hour"));
        card.add(Box.createVerticalStrut(16));
        card.add(UITheme.formRow("Major Service (RM)",  spMajor));
        card.add(Box.createVerticalStrut(4));
        card.add(UITheme.mutedLabel("  Duration: 3 hours"));
        card.add(Box.createVerticalStrut(28));

        lblMsg = new JLabel(" ");
        lblMsg.setName("lblMsg");
        lblMsg.setFont(UITheme.FONT_SMALL);
        lblMsg.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblMsg);
        card.add(Box.createVerticalStrut(8));

        JButton btnSave = UITheme.accentButton("💾  Save Prices");
        btnSave.setName("btnSave");
        btnSave.setAlignmentX(LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> doSave());
        card.add(btnSave);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(24, 80, 24, 80);
        add(card, gbc);
    }

    private void styleSpinner(JSpinner sp) {
        sp.setBackground(UITheme.FIELD_BG);
        sp.setForeground(UITheme.TEXT_PRIMARY);
        sp.setFont(UITheme.FONT_BODY);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setBackground(UITheme.FIELD_BG);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setForeground(UITheme.TEXT_PRIMARY);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setFont(UITheme.FONT_BODY);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.FIELD_BORDER, 1));
    }

    private void doSave() {
        double normalPrice = (Double) spNormal.getValue();
        double majorPrice = (Double) spMajor.getValue();
        if (normalPrice <= 0 || majorPrice <= 0) {
            lblMsg.setText("Prices must be greater than zero.");
            lblMsg.setForeground(UITheme.ACCENT);
            return;
        }
        PaymentService.setServicePrice("Normal", normalPrice);
        PaymentService.setServicePrice("Major",  majorPrice);
        lblMsg.setText("✔ Prices saved: Normal RM " + String.format("%.2f", normalPrice) + ", Major RM " + String.format("%.2f", majorPrice));
        lblMsg.setForeground(UITheme.SUCCESS);
    }
}
