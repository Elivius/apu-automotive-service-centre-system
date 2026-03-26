package ui.customer;

import models.Customer;
import ui.EditProfileFrame;
import ui.LoginFrame;
import ui.ToastNotification;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Main dashboard frame for the Customer role.
 * Uses a sidebar + CardLayout content area pattern.
 * Implements displayDashboard() via polymorphism from User.
 */
public class CustomerDashboard extends JFrame {

    private final Customer customer;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     contentArea = new JPanel(cardLayout);

    private static final String PANEL_BOOK     = "book";
    private static final String PANEL_APPTS    = "appointments";
    private static final String PANEL_HISTORY  = "history";
    private static final String PANEL_PAYMENTS = "payments";

    public CustomerDashboard(Customer customer) {
        this.customer = customer;
        setTitle("APU-ASC — Customer Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 580));
        getContentPane().setBackground(UITheme.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // ── Sidebar ───────────────────────────────────────────────────
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // ── Content panels ────────────────────────────────────────────
        contentArea.setBackground(UITheme.BG_DARK);
        contentArea.add(new BookAppointmentPanel(customer), PANEL_BOOK);
        contentArea.add(new MyAppointmentsPanel(customer),  PANEL_APPTS);
        contentArea.add(new ServiceHistoryPanel(customer),  PANEL_HISTORY);
        contentArea.add(new PaymentHistoryPanel(customer),  PANEL_PAYMENTS);
        add(contentArea, BorderLayout.CENTER);

        // Show default panel
        cardLayout.show(contentArea, PANEL_APPTS);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        // Avatar / name area
        JLabel avatar = new JLabel("👤", SwingConstants.CENTER);
        avatar.setName("avatar");
        avatar.setFont(new Font("SansSerif", Font.PLAIN, 36));
        avatar.setAlignmentX(CENTER_ALIGNMENT);

        JLabel name = new JLabel(customer.getName(), SwingConstants.CENTER);
        name.setName("name");
        name.setFont(UITheme.FONT_HEADER);
        name.setForeground(UITheme.TEXT_PRIMARY);
        name.setAlignmentX(CENTER_ALIGNMENT);

        JLabel role = UITheme.mutedLabel("Customer  •  " + customer.getUserId());
        role.setName("role");
        role.setAlignmentX(CENTER_ALIGNMENT);
        role.setHorizontalAlignment(SwingConstants.CENTER);

        sidebar.add(avatar);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(name);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(role);
        sidebar.add(Box.createVerticalStrut(28));

        JSeparator sep = new JSeparator(); sep.setForeground(new Color(0x1E4080));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(16));

        // Navigation buttons
        sidebar.add(sidebarBtn("📋  My Appointments",  () -> cardLayout.show(contentArea, PANEL_APPTS)));
        sidebar.add(sidebarBtn("➕  Book Appointment",  () -> {
            // Refresh panel then switch
            contentArea.remove(contentArea.getComponent(0));
            contentArea.add(new BookAppointmentPanel(customer), PANEL_BOOK, 0);
            cardLayout.show(contentArea, PANEL_BOOK);
        }));
        sidebar.add(sidebarBtn("🔧  Service History",   () -> cardLayout.show(contentArea, PANEL_HISTORY)));
        sidebar.add(sidebarBtn("💳  Payment History",   () -> cardLayout.show(contentArea, PANEL_PAYMENTS)));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarBtn("✏️  Edit Profile",       () -> new EditProfileFrame(customer).setVisible(true)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sidebarBtn("🚪  Logout",            this::doLogout));

        return sidebar;
    }

    private JButton sidebarBtn(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0x1E4080));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        btn.addActionListener(e -> action.run());
        btn.setName("btn" + text);
        
        return btn;
    }

    private void doLogout() {
        int ok = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
