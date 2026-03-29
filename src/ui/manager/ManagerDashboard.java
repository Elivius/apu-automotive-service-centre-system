package ui.manager;

import models.Manager;
import ui.EditProfileFrame;
import ui.LoginFrame;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Main dashboard frame for the Manager role.
 */
public class ManagerDashboard extends JFrame {

    private final Manager manager;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);

    private static final String PANEL_STAFF    = "staff";
    private static final String PANEL_PRICES   = "prices";
    private static final String PANEL_REPORTS  = "reports";
    private static final String PANEL_FEEDBACK = "feedback";

    public ManagerDashboard(Manager manager) {
        this.manager = manager;
        setTitle("APU-ASC — Manager Portal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 620));
        getContentPane().setBackground(UITheme.BG_DARK);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);

        contentArea.setBackground(UITheme.BG_DARK);
        
        // Initial panels with names for the switchTab logic
        addNamedPanel(new ManageStaffPanel(), PANEL_STAFF);
        addNamedPanel(new ServicePricesPanel(), PANEL_PRICES);
        addNamedPanel(new ReportsPanel(), PANEL_REPORTS);
        addNamedPanel(new AllFeedbackPanel(), PANEL_FEEDBACK);
        
        add(contentArea, BorderLayout.CENTER);
        cardLayout.show(contentArea, PANEL_REPORTS);
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

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        JLabel avatar = new JLabel("📊", SwingConstants.CENTER);
        avatar.setName("lblAvatar");
        avatar.setFont(new Font("SansSerif", Font.PLAIN, 36));
        avatar.setAlignmentX(CENTER_ALIGNMENT);

        JLabel name = new JLabel(manager.getName(), SwingConstants.CENTER);
        name.setName("lblName");
        name.setFont(UITheme.FONT_HEADER);
        name.setForeground(UITheme.TEXT_PRIMARY);
        name.setAlignmentX(CENTER_ALIGNMENT);

        JLabel role = UITheme.mutedLabel("Manager  •  " + manager.getUserId());
        role.setName("lblRole");
        role.setAlignmentX(CENTER_ALIGNMENT);
        role.setHorizontalAlignment(SwingConstants.CENTER);

        sidebar.add(avatar);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(name);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(role);
        sidebar.add(Box.createVerticalStrut(28));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x1E4080));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(16));

        sidebar.add(sidebarBtn("📈  Reports",        () -> switchTab(PANEL_REPORTS,  ReportsPanel::new)));
        sidebar.add(sidebarBtn("👥  Manage Staff",     () -> switchTab(PANEL_STAFF,    ManageStaffPanel::new)));
        sidebar.add(sidebarBtn("💲  Service Prices",   () -> switchTab(PANEL_PRICES,   ServicePricesPanel::new)));
        sidebar.add(sidebarBtn("💬  All Feedback",     () -> switchTab(PANEL_FEEDBACK, AllFeedbackPanel::new)));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarBtn("✏️  Edit Profile", () -> new EditProfileFrame(manager).setVisible(true)));
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
                    g2.fillRect(0,0,getWidth(),getHeight());
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
        return btn;
    }

    private void doLogout() {
        int ok = JOptionPane.showConfirmDialog(this, "Logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
