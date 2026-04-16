package ui.customer;

import models.Customer;
import services.NotificationService;
import ui.EditProfileFrame;
import ui.LoginFrame;
import ui.NotificationPanel;
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
    private final JPanel contentArea = new JPanel(cardLayout);
    private JLabel lblName;

    private static final String PANEL_BOOK = "book";
    private static final String PANEL_APPTS = "appointments";
    private static final String PANEL_HISTORY = "history";
    private static final String PANEL_PAYMENTS = "payments";

    // Active-state refs
    private final boolean[] activeAppts = {true};
    private final boolean[] activeBook = {false};
    private final boolean[] activeHistory = {false};
    private final boolean[] activePayments = {false};

    private JButton btnAppts, btnBook, btnHistory, btnPayments;

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
        add(buildSidebar(), BorderLayout.WEST);
        add(buildTopBar(), BorderLayout.NORTH);

        contentArea.setBackground(UITheme.BG_DARK);
        addNamedPanel(new MyAppointmentsPanel(customer), PANEL_APPTS);
        addNamedPanel(new BookAppointmentPanel(customer), PANEL_BOOK);
        addNamedPanel(new ServiceHistoryPanel(customer), PANEL_HISTORY);
        addNamedPanel(new PaymentHistoryPanel(customer), PANEL_PAYMENTS);

        add(contentArea, BorderLayout.CENTER);

        // Show default panel
        cardLayout.show(contentArea, PANEL_APPTS);
    }

    private void addNamedPanel(JPanel panel, String name) {
        panel.setName(name);
        contentArea.add(panel, name);
    }

    private void switchTab(String name, java.util.function.Supplier<JPanel> supplier) {
        activeAppts[0] = PANEL_APPTS.equals(name);
        activeBook[0] = PANEL_BOOK.equals(name);
        activeHistory[0] = PANEL_HISTORY.equals(name);
        activePayments[0] = PANEL_PAYMENTS.equals(name);

        for (JButton b : new JButton[]{btnAppts, btnBook, btnHistory, btnPayments}) {
            if (b != null) b.repaint();
        }

        Component[] components = contentArea.getComponents();
        for (Component component : components) {
            if (name.equals(component.getName())) {
                contentArea.remove(component);
                break;
            }
        }
        JPanel freshPanel = supplier.get();
        freshPanel.setName(name);
        contentArea.add(freshPanel, name);
        cardLayout.show(contentArea, name);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ── Top Bar ─────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, getHeight() - 1, UITheme.ACCENT,
                                                      getWidth(), getHeight() - 1, UITheme.ACCENT_SECONDARY);
                g2.setPaint(gp);
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        topBar.setBackground(UITheme.BG_CARD);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblPageTitle = new JLabel("Customer Dashboard");
        lblPageTitle.setName("lblPageTitle");
        lblPageTitle.setFont(UITheme.FONT_HEADER);
        lblPageTitle.setForeground(UITheme.TEXT_PRIMARY);
        topBar.add(lblPageTitle, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(createBellButton());
        topBar.add(rightPanel, BorderLayout.EAST);
        return topBar;
    }

    private JButton createBellButton() {
        int unread = NotificationService.getUnreadCount(customer.getUserId(), customer.getRole());
        JButton btnBell = new JButton("🔔") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(UITheme.BG_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBell.setName("btnNotificationBell");
        btnBell.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btnBell.setOpaque(false);
        btnBell.setContentAreaFilled(false);
        btnBell.setBorderPainted(false);
        btnBell.setFocusPainted(false);
        btnBell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBell.setPreferredSize(new Dimension(80, 36));
        updateBellText(btnBell, unread);
        btnBell.addActionListener(e -> NotificationPanel.show(btnBell, customer, () -> {
            int count = NotificationService.getUnreadCount(customer.getUserId(), customer.getRole());
            updateBellText(btnBell, count);
        }));
        return btnBell;
    }

    private void updateBellText(JButton btnBell, int count) {
        if (count > 0) {
            btnBell.setText("🔔 " + count);
            btnBell.setForeground(UITheme.ACCENT);
        } else {
            btnBell.setText("🔔");
            btnBell.setForeground(UITheme.TEXT_MUTED);
        }
    }

    // ── Gradient Sidebar ─────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0, UITheme.BG_SIDEBAR,
                    0, getHeight(), new Color(0x0A0B1A));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_CARD);
                g2.fillRect(getWidth() - 1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(28, 0, 24, 0));

        JLabel avatar = UITheme.avatarLabel("👤", 56);
        avatar.setName("lblAvatar");
        avatar.setAlignmentX(CENTER_ALIGNMENT);

        lblName = new JLabel(customer.getName(), SwingConstants.CENTER);
        lblName.setName("lblName");
        lblName.setFont(UITheme.FONT_HEADER);
        lblName.setForeground(UITheme.TEXT_PRIMARY);
        lblName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblRole = UITheme.mutedLabel("Customer  •  " + customer.getUserId());
        lblRole.setName("lblRole");
        lblRole.setAlignmentX(CENTER_ALIGNMENT);
        lblRole.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Avatar / user section — FlowLayout wrapper guarantees centering
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarRow.setOpaque(false);
        avatarRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        avatarRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        avatarRow.add(avatar);

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        nameRow.add(lblName);

        JPanel roleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        roleRow.setOpaque(false);
        roleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        roleRow.add(lblRole);

        sidebar.add(avatarRow);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(nameRow);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(roleRow);
        sidebar.add(Box.createVerticalStrut(28));

        JSeparator sep = UITheme.sectionDivider();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(16));

        btnAppts = UITheme.sidebarButton("📋  My Appointments", () -> switchTab(PANEL_APPTS, () -> new MyAppointmentsPanel(customer)), activeAppts);
        btnBook = UITheme.sidebarButton("➕  Book Appointment", () -> switchTab(PANEL_BOOK, () -> new BookAppointmentPanel(customer)), activeBook);
        btnHistory = UITheme.sidebarButton("🔧  Service History", () -> switchTab(PANEL_HISTORY, () -> new ServiceHistoryPanel(customer)), activeHistory);
        btnPayments = UITheme.sidebarButton("💳  Payment History", () -> switchTab(PANEL_PAYMENTS, () -> new PaymentHistoryPanel(customer)), activePayments);

        btnAppts.setName("btnMyAppointments");
        btnBook.setName("btnBookAppointment");
        btnHistory.setName("btnServiceHistory");
        btnPayments.setName("btnPaymentHistory");

        sidebar.add(btnAppts);
        sidebar.add(btnBook);
        sidebar.add(btnHistory);
        sidebar.add(btnPayments);
        sidebar.add(Box.createVerticalGlue());

        JSeparator sep2 = UITheme.sectionDivider();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sidebar.add(sep2);
        sidebar.add(Box.createVerticalStrut(12));

        // Put dummy to make it always false because these 2 buttons should not having the active state
        boolean[] dummy1 = {false}, dummy2 = {false};
        JButton btnEdit = UITheme.sidebarButton("✏️  Edit Profile", () -> new EditProfileFrame(customer, () -> lblName.setText(customer.getName())).setVisible(true), dummy1);
        JButton btnLogout = UITheme.sidebarButton("🚪  Logout", this::doLogout, dummy2);
        btnEdit.setName("btnEditProfile");
        btnLogout.setName("btnLogout");

        sidebar.add(btnEdit);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnLogout);
        return sidebar;
    }

    private void doLogout() {
        int ok = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
