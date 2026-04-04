package ui.technician;

import models.Technician;
import services.NotificationService;
import ui.EditProfileFrame;
import ui.LoginFrame;
import ui.NotificationPanel;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Main dashboard frame for the Technician role.
 */
public class TechnicianDashboard extends JFrame {

    private final Technician technician;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);
    private JLabel lblName;
    private JLabel lblBadge;

    private static final String PANEL_APPTS = "appointments";

    public TechnicianDashboard(Technician technician) {
        this.technician = technician;
        setTitle("APU-ASC — Technician Portal");
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
        
        // Initial panels with names for the switchTab logic
        addNamedPanel(new TechMyAppointmentsPanel(technician), PANEL_APPTS);
        
        add(contentArea, BorderLayout.CENTER);
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

        JLabel lblPageTitle = new JLabel("Technician Dashboard");
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
        int unread = NotificationService.getUnreadCount(technician.getUserId(), technician.getRole());

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
            NotificationPanel.show(btnBell, technician, () -> {
                int count = NotificationService.getUnreadCount(technician.getUserId(), technician.getRole());
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

        JLabel lblAvatar = new JLabel("🔧", SwingConstants.CENTER);
        setName("lblAvatar");
        lblAvatar.setFont(new Font("SansSerif", Font.PLAIN, 36));
        lblAvatar.setAlignmentX(CENTER_ALIGNMENT);
        
        lblName = new JLabel(technician.getName(), SwingConstants.CENTER);
        setName("lblName");
        lblName.setFont(UITheme.FONT_HEADER); 
        lblName.setForeground(UITheme.TEXT_PRIMARY);
        lblName.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel lblRole = UITheme.mutedLabel("Technician  •  " + technician.getUserId());
        setName("lblRole");
        lblRole.setAlignmentX(CENTER_ALIGNMENT); 
        lblRole.setHorizontalAlignment(SwingConstants.CENTER);

        String spec = technician.getSpecialization();
        if (spec != null && !spec.isEmpty()) {
            JLabel specLabel = UITheme.mutedLabel("Spec: " + spec);
            specLabel.setAlignmentX(CENTER_ALIGNMENT);
            specLabel.setHorizontalAlignment(SwingConstants.CENTER);
            sidebar.add(Box.createVerticalStrut(2));
            sidebar.add(specLabel);
        }

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

        sidebar.add(sidebarBtn("📋  My Appointments", () -> switchTab(PANEL_APPTS, () -> new TechMyAppointmentsPanel(technician))));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarBtn("✏️  Edit Profile", () -> new EditProfileFrame(technician, () -> lblName.setText(technician.getName())).setVisible(true)));
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
