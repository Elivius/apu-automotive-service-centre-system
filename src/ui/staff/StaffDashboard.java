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
    private final JPanel     contentArea = new JPanel(cardLayout);

    private static final String PANEL_CUSTOMERS    = "customers";
    private static final String PANEL_APPOINTMENTS = "appointments";
    private static final String PANEL_PAYMENTS     = "payments";

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
        contentArea.add(new ManageAppointmentsPanel(staff), PANEL_APPOINTMENTS);
        contentArea.add(new ManageCustomersPanel(staff),    PANEL_CUSTOMERS);
        contentArea.add(new CollectPaymentPanel(staff),     PANEL_PAYMENTS);
        add(contentArea, BorderLayout.CENTER);

        cardLayout.show(contentArea, PANEL_APPOINTMENTS);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        JLabel avatar   = new JLabel("🧑‍💼", SwingConstants.CENTER);
        avatar.setFont(new Font("SansSerif", Font.PLAIN, 36));
        avatar.setAlignmentX(CENTER_ALIGNMENT);
        JLabel name = new JLabel(staff.getName(), SwingConstants.CENTER);
        name.setFont(UITheme.FONT_HEADER); name.setForeground(UITheme.TEXT_PRIMARY);
        name.setAlignmentX(CENTER_ALIGNMENT);
        JLabel role = UITheme.mutedLabel("Counter Staff  •  " + staff.getUserId());
        role.setAlignmentX(CENTER_ALIGNMENT); role.setHorizontalAlignment(SwingConstants.CENTER);

        sidebar.add(avatar); sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(name);   sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(role);   sidebar.add(Box.createVerticalStrut(28));
        JSeparator sep = new JSeparator(); sep.setForeground(new Color(0x1E4080));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep); sidebar.add(Box.createVerticalStrut(16));

        sidebar.add(sidebarBtn("📅  Appointments",  () -> cardLayout.show(contentArea, PANEL_APPOINTMENTS)));
        sidebar.add(sidebarBtn("👥  Customers",      () -> cardLayout.show(contentArea, PANEL_CUSTOMERS)));
        sidebar.add(sidebarBtn("💳  Collect Payment",() -> cardLayout.show(contentArea, PANEL_PAYMENTS)));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarBtn("✏️  Edit Profile",   () -> new EditProfileFrame(staff).setVisible(true)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sidebarBtn("🚪  Logout",         this::doLogout));
        return sidebar;
    }

    private JButton sidebarBtn(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (getModel().isRollover()) { g2.setColor(new Color(0x1E4080)); g2.fillRect(0,0,getWidth(),getHeight()); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(UITheme.FONT_BODY); btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT); btn.setOpaque(false);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void doLogout() {
        int ok = JOptionPane.showConfirmDialog(this,"Logout?","Logout",JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) { dispose(); new LoginFrame().setVisible(true); }
    }
}
