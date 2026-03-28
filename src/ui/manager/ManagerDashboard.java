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
        contentArea.add(new ManageStaffPanel(), PANEL_STAFF);
        contentArea.add(new ServicePricesPanel(), PANEL_PRICES);
        contentArea.add(new ReportsPanel(), PANEL_REPORTS);
        contentArea.add(new AllFeedbackPanel(), PANEL_FEEDBACK);
        add(contentArea, BorderLayout.CENTER);
        cardLayout.show(contentArea, PANEL_REPORTS);
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

        sidebar.add(sidebarBtn("📈  Reports", () -> {
            contentArea.remove(0);
            contentArea.add(new ReportsPanel(), PANEL_REPORTS, 0);
            cardLayout.show(contentArea, PANEL_REPORTS);
        }));
        sidebar.add(sidebarBtn("👥  Manage Staff", () -> cardLayout.show(contentArea, PANEL_STAFF)));
        sidebar.add(sidebarBtn("💲  Service Prices", () -> cardLayout.show(contentArea, PANEL_PRICES)));
        sidebar.add(sidebarBtn("💬  All Feedback", () -> cardLayout.show(contentArea, PANEL_FEEDBACK)));
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
