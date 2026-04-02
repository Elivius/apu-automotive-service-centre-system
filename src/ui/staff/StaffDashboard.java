package ui.staff;

import models.CounterStaff;
import ui.EditProfileFrame;
import ui.LoginFrame;
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

        contentArea.setBackground(UITheme.BG_DARK);

        // Initial panels with names for the switchTab logic
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

        JLabel lblAvatar = new JLabel("🧑‍💼", SwingConstants.CENTER);
        setName("lblAvatar");
        lblAvatar.setFont(new Font("SansSerif", Font.PLAIN, 36));
        lblAvatar.setAlignmentX(CENTER_ALIGNMENT);
        
        lblName = new JLabel(staff.getName(), SwingConstants.CENTER);
        setName("lblName");
        lblName.setFont(UITheme.FONT_HEADER); 
        lblName.setForeground(UITheme.TEXT_PRIMARY);
        lblName.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel lblRole = UITheme.mutedLabel("Counter Staff  •  " + staff.getUserId());
        setName("lblRole");
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

        sidebar.add(sidebarBtn("📅  Appointments", () -> switchTab(PANEL_APPOINTMENTS, () -> new ManageAppointmentsPanel(staff))));
        sidebar.add(sidebarBtn("👥  Customers", () -> switchTab(PANEL_CUSTOMERS, () -> new ManageCustomersPanel(staff))));
        sidebar.add(sidebarBtn("💳  Collect Payment", () -> switchTab(PANEL_PAYMENTS, () -> new CollectPaymentPanel(staff))));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarBtn("✏️  Edit Profile", () -> new EditProfileFrame(staff, () -> lblName.setText(staff.getName())).setVisible(true)));
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
