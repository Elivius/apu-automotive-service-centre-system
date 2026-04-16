package ui.staff;

import models.CounterStaff;
import services.NotificationService;
import ui.EditProfileFrame;
import ui.LoginFrame;
import ui.NotificationPanel;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Main dashboard frame for the Counter Staff role.
 */
public class StaffDashboard extends JFrame {

    private final CounterStaff staff;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);
    private JLabel lblName;

    private static final String PANEL_CUSTOMERS = "customers";
    private static final String PANEL_APPOINTMENTS = "appointments";
    private static final String PANEL_PAYMENTS = "payments";

    // Active-state refs
    private final boolean[] activeAppts = {true};
    private final boolean[] activeCustomers = {false};
    private final boolean[] activePayments = {false};

    private JButton btnAppts, btnCustomers, btnPayments;

    public StaffDashboard(CounterStaff staff) {
        this.staff = staff;
        setTitle("APU-ASC — Counter Staff Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 740);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
        getContentPane().setBackground(UITheme.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildTopBar(), BorderLayout.NORTH);

        contentArea.setBackground(UITheme.BG_DARK);
        addNamedPanel(new ManageAppointmentsPanel(staff), PANEL_APPOINTMENTS);
        addNamedPanel(new ManageCustomersPanel(staff), PANEL_CUSTOMERS);
        addNamedPanel(new CollectPaymentPanel(staff), PANEL_PAYMENTS);

        add(contentArea, BorderLayout.CENTER);
        cardLayout.show(contentArea, PANEL_APPOINTMENTS);
    }

    private void addNamedPanel(JPanel panel, String name) {
        panel.setName(name);
        contentArea.add(panel, name);
    }

    private void switchTab(String name, java.util.function.Supplier<JPanel> supplier) {
        activeAppts[0] = PANEL_APPOINTMENTS.equals(name);
        activeCustomers[0] = PANEL_CUSTOMERS.equals(name);
        activePayments[0] = PANEL_PAYMENTS.equals(name);

        for (JButton button : new JButton[]{btnAppts, btnCustomers, btnPayments}) {
            if (button != null) button.repaint();
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
            @Override
            protected void paintComponent(Graphics g) {
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

        JLabel lblPageTitle = new JLabel("Counter Staff Dashboard");
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
        int unread = NotificationService.getUnreadCount(staff.getUserId(), staff.getRole());
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
        btnBell.addActionListener(e -> NotificationPanel.show(btnBell, staff, () -> {
            int count = NotificationService.getUnreadCount(staff.getUserId(), staff.getRole());
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

        JLabel avatar = UITheme.avatarLabel("🧑‍💼", 56);
        avatar.setName("lblAvatar");
        avatar.setAlignmentX(CENTER_ALIGNMENT);

        lblName = new JLabel(staff.getName(), SwingConstants.CENTER);
        lblName.setName("lblName");
        lblName.setFont(UITheme.FONT_HEADER);
        lblName.setForeground(UITheme.TEXT_PRIMARY);
        lblName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblRole = UITheme.mutedLabel("Counter Staff  •  " + staff.getUserId());
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

        btnAppts = UITheme.sidebarButton("📅  Appointments", () -> switchTab(PANEL_APPOINTMENTS, () -> new ManageAppointmentsPanel(staff)), activeAppts);
        btnCustomers = UITheme.sidebarButton("👥  Customers", () -> switchTab(PANEL_CUSTOMERS, () -> new ManageCustomersPanel(staff)), activeCustomers);
        btnPayments = UITheme.sidebarButton("💳  Collect Payment", () -> switchTab(PANEL_PAYMENTS, () -> new CollectPaymentPanel(staff)), activePayments);

        btnAppts.setName("btnAppointments");
        btnCustomers.setName("btnCustomers");
        btnPayments.setName("btnCollectPayment");

        sidebar.add(btnAppts);
        sidebar.add(btnCustomers);
        sidebar.add(btnPayments);
        sidebar.add(Box.createVerticalGlue());

        JSeparator sep2 = UITheme.sectionDivider();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sidebar.add(sep2);
        sidebar.add(Box.createVerticalStrut(12));

        boolean[] dummy1 = {false}, dummy2 = {false};
        JButton btnEdit = UITheme.sidebarButton("✏️  Edit Profile", () -> new EditProfileFrame(staff, () -> lblName.setText(staff.getName())).setVisible(true), dummy1);
        JButton btnLogout = UITheme.sidebarButton("🚪  Logout", this::doLogout, dummy2);
        btnEdit.setName("btnEditProfile");
        btnLogout.setName("btnLogout");

        sidebar.add(btnEdit);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(btnLogout);
        return sidebar;
    }

    private void doLogout() {
        int ok = JOptionPane.showConfirmDialog(this, "Logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
