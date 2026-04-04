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
    private JLabel lblBadge;

    private static final String PANEL_BOOK = "book";
    private static final String PANEL_APPTS = "appointments";
    private static final String PANEL_HISTORY = "history";
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

        // ── Top Bar ───────────────────────────────────────────────────
        add(buildTopBar(), BorderLayout.NORTH);

        // ── Content panels ────────────────────────────────────────────
        contentArea.setBackground(UITheme.BG_DARK);
        
        // Initial panels with names for the switchTab logic
        addNamedPanel(new BookAppointmentPanel(customer), PANEL_BOOK);
        addNamedPanel(new MyAppointmentsPanel(customer), PANEL_APPTS);
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

    // ── Top Bar with Notification Bell ──────────────────────────────────
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_CARD);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.FIELD_BORDER),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));

        JLabel lblPageTitle = new JLabel("Customer Dashboard");
        lblPageTitle.setName("lblPageTitle");
        lblPageTitle.setFont(UITheme.FONT_HEADER);
        lblPageTitle.setForeground(UITheme.TEXT_PRIMARY);
        topBar.add(lblPageTitle, BorderLayout.WEST);

        // Bell button with badge
        JPanel bellPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bellPanel.setOpaque(false);

        JButton btnBell = createBellButton();
        bellPanel.add(btnBell);

        topBar.add(bellPanel, BorderLayout.EAST);
        return topBar;
    }

    private JButton createBellButton() {
        int unread = NotificationService.getUnreadCount(customer.getUserId(), customer.getRole());

        lblBadge = new JLabel(String.valueOf(unread));
        lblBadge.setName("lblNotificationBadge");

        JButton btnBell = new JButton("🔔") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0x1E4080));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBell.setName("btnNotificationBell");
        btnBell.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btnBell.setForeground(UITheme.TEXT_PRIMARY);
        btnBell.setOpaque(false);
        btnBell.setContentAreaFilled(false);
        btnBell.setBorderPainted(false);
        btnBell.setFocusPainted(false);
        btnBell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBell.setPreferredSize(new Dimension(80, 36));

        updateBellText(btnBell, unread);

        btnBell.addActionListener(e -> {
            NotificationPanel.show(btnBell, customer, () -> {
                int count = NotificationService.getUnreadCount(customer.getUserId(), customer.getRole());
                updateBellText(btnBell, count);
            });
        });

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

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        // Avatar / name area
        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblAvatar.setName("lblAvatar");
        lblAvatar.setFont(new Font("SansSerif", Font.PLAIN, 36));
        lblAvatar.setAlignmentX(CENTER_ALIGNMENT);

        lblName = new JLabel(customer.getName(), SwingConstants.CENTER);
        lblName.setName("lblName");
        lblName.setFont(UITheme.FONT_HEADER);
        lblName.setForeground(UITheme.TEXT_PRIMARY);
        lblName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblRole = UITheme.mutedLabel("Customer  •  " + customer.getUserId());
        lblRole.setName("lblRole");
        lblRole.setAlignmentX(CENTER_ALIGNMENT);
        lblRole.setHorizontalAlignment(SwingConstants.CENTER);

        sidebar.add(lblAvatar);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(lblName);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(lblRole);
        sidebar.add(Box.createVerticalStrut(28));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x1E4080));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(16));

        // Navigation buttons
        sidebar.add(sidebarBtn("📋  My Appointments", () -> switchTab(PANEL_APPTS, () -> new MyAppointmentsPanel(customer))));
        sidebar.add(sidebarBtn("➕  Book Appointment", () -> switchTab(PANEL_BOOK, () -> new BookAppointmentPanel(customer))));
        sidebar.add(sidebarBtn("🔧  Service History", () -> switchTab(PANEL_HISTORY, () -> new ServiceHistoryPanel(customer))));
        sidebar.add(sidebarBtn("💳  Payment History", () -> switchTab(PANEL_PAYMENTS, () -> new PaymentHistoryPanel(customer))));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarBtn("✏️  Edit Profile", () -> new EditProfileFrame(customer, () -> lblName.setText(customer.getName())).setVisible(true)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sidebarBtn("🚪  Logout", this::doLogout));

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
        int ok = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
