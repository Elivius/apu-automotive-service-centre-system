package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Centralised design system for the APU-ASC application.
 * Provides colours, fonts, and helper factory methods so
 * every screen looks consistent without repeating style code.
 */
public class UITheme {

    // ─── Colour Palette ────────────────────────────────────────────────
    public static final Color BG_DARK           = new Color(0x0E0F1A);
    public static final Color BG_CARD           = new Color(0x161728);
    public static final Color BG_SIDEBAR        = new Color(0x12132A);
    public static final Color BG_HOVER          = new Color(0x1E2042);
    public static final Color ACCENT            = new Color(0x7C6BFF);
    public static final Color ACCENT_HOVER      = new Color(0x9D90FF);
    public static final Color ACCENT_SECONDARY  = new Color(0x38BDF8);
    public static final Color BORDER_CARD       = new Color(0x2A2B4A);
    public static final Color TEXT_PRIMARY      = new Color(0xEAEAEA);
    public static final Color TEXT_MUTED        = new Color(0x8B8FA8);
    public static final Color SUCCESS           = new Color(0x10B981);
    public static final Color WARNING           = new Color(0xF59E0B);
    public static final Color DANGER            = new Color(0xEF4444);
    public static final Color TABLE_HEADER      = new Color(0x1A1B35);
    public static final Color TABLE_ALT_ROW     = new Color(0x1A1C34);
    public static final Color FIELD_BG          = new Color(0x0D0E1F);
    public static final Color FIELD_BORDER      = new Color(0x2A2B4A);
    public static final Color FIELD_FOCUS       = new Color(0x7C6BFF);

    // ─── Fonts ─────────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD,  24);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD,  15);
    public static final Font FONT_BODY   = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD,  13);

    // ─── Global Defaults ───────────────────────────────────────────────
    public static void applyGlobalDefaults() {
        UIManager.put("OptionPane.background",          BG_CARD);
        UIManager.put("Panel.background",               BG_DARK);
        UIManager.put("OptionPane.messageForeground",   TEXT_PRIMARY);
        UIManager.put("Button.background",              ACCENT);
        UIManager.put("Button.foreground",              Color.WHITE);
        UIManager.put("Button.font",                    FONT_BUTTON);
        UIManager.put("Label.foreground",               TEXT_PRIMARY);
        UIManager.put("Label.font",                     FONT_BODY);
        UIManager.put("TextField.background",           FIELD_BG);
        UIManager.put("TextField.foreground",           TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",      TEXT_PRIMARY);
        UIManager.put("PasswordField.background",       FIELD_BG);
        UIManager.put("PasswordField.foreground",       TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground",  TEXT_PRIMARY);
        UIManager.put("ComboBox.background",            FIELD_BG);
        UIManager.put("ComboBox.foreground",            TEXT_PRIMARY);
        UIManager.put("TextArea.background",            FIELD_BG);
        UIManager.put("TextArea.foreground",            TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground",       TEXT_PRIMARY);
        UIManager.put("ScrollPane.background",          BG_DARK);
        UIManager.put("Viewport.background",            BG_DARK);
        UIManager.put("Table.background",               BG_CARD);
        UIManager.put("Table.foreground",               TEXT_PRIMARY);
        UIManager.put("Table.gridColor",                FIELD_BORDER);
        UIManager.put("Table.selectionBackground",      ACCENT);
        UIManager.put("Table.selectionForeground",      Color.WHITE);
        UIManager.put("TableHeader.background",         TABLE_HEADER);
        UIManager.put("TableHeader.foreground",         Color.WHITE);
        UIManager.put("TableHeader.font",               FONT_HEADER);
        UIManager.put("TabbedPane.background",          BG_CARD);
        UIManager.put("TabbedPane.foreground",          TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected",            BG_HOVER);
        UIManager.put("Spinner.background",             FIELD_BG);
        UIManager.put("Spinner.foreground",             TEXT_PRIMARY);
    }

    // ─── Label Factories ───────────────────────────────────────────────

    /** A bold section header label. */
    public static JLabel headerLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    /** A large page-title label. */
    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    /** Small muted label for hints/captions. */
    public static JLabel mutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    // ─── Button Factories ──────────────────────────────────────────────

    /** Standard accent-coloured rounded gradient button. */
    public static JButton accentButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = getModel().isRollover() ? ACCENT_HOVER : ACCENT;
                Color c2 = getModel().isRollover() ? new Color(0xB8AFFF) : new Color(0x5A4FD6);
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 28, 40));
        return btn;
    }

    /** Outline-style secondary button using sky-blue accent. */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? new Color(0x1E2F45) : BG_CARD;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(ACCENT_SECONDARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(ACCENT_SECONDARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, 36));
        return btn;
    }

    /** Danger (red) button for destructive actions. */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xFF6666) : DANGER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
            {
                setFont(FONT_BUTTON);
                setForeground(Color.WHITE);
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(getPreferredSize().width + 24, 38));
            }
        };
        return btn;
    }

    // ─── Field Factories ───────────────────────────────────────────────

    /** A styled JTextField with focus glow effect. */
    public static JTextField styledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        applyFieldStyle(tf);
        return tf;
    }

    /** A styled JPasswordField with focus glow effect. */
    public static JPasswordField styledPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        applyFieldStyle(pf);
        return pf;
    }

    /** Apply consistent dark styling + focus glow to any JTextComponent. */
    public static void applyFieldStyle(JTextComponent field) {
        field.setBackground(FIELD_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FIELD_FOCUS, 2, true),
                    BorderFactory.createEmptyBorder(6, 11, 6, 11)));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)));
            }
        });
    }

    /** A styled JTextArea wrapped in a scroll pane. */
    public static JScrollPane styledTextArea(JTextArea ta) {
        ta.setBackground(FIELD_BG);
        ta.setForeground(TEXT_PRIMARY);
        ta.setCaretColor(ACCENT);
        ta.setFont(FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(BorderFactory.createLineBorder(FIELD_BORDER, 1));
        sp.getViewport().setBackground(FIELD_BG);
        return sp;
    }

    // ─── Panel Factories ───────────────────────────────────────────────

    /**
     * A premium card-style panel: gradient fill, rounded corners (20px),
     * subtle border, and a soft painted drop-shadow beneath.
     */
    public static JPanel cardPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 6, 20, 20);
                // Card gradient fill
                GradientPaint gp = new GradientPaint(
                    0, 0, BG_CARD,
                    0, getHeight(), new Color(0x1C1E38));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
                // Card border
                g2.setColor(BORDER_CARD);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
                // Accent top stripe
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth() - 4, 5, 20, 20);
                g2.fillRect(0, 2, getWidth() - 4, 3);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    // ─── Table Factory ─────────────────────────────────────────────────

    /** A styled JScrollPane wrapping a JTable with alternating rows. */
    public static JScrollPane styledTable(JTable table) {
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_CARD);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        // Alternating row renderer (Zebra stripping)
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? BG_CARD : TABLE_ALT_ROW);
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        });

        table.getTableHeader().setFont(FONT_HEADER);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_CARD, 1));
        return sp;
    }

    // ─── Sidebar Button ────────────────────────────────────────────────

    /**
     * Shared sidebar navigation button for all dashboards.
     * Pass a single-element boolean array as activeRef so the caller can
     * toggle it and call repaint() to update the active state indicator.
     *
     * Usage: boolean[] isActive = {false};
     *        JButton btn = UITheme.sidebarButton("📋  My Stuff", action, isActive);
     */
    public static JButton sidebarButton(String text, Runnable action, boolean[] activeRef) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (activeRef[0]) {
                    // Active: highlighted background + left accent bar
                    g2.setColor(BG_HOVER);
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 10, 10);
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(2, 8, 4, getHeight() - 16, 4, 4);
                    setForeground(TEXT_PRIMARY);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0x181940));
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 10, 10);
                    setForeground(TEXT_PRIMARY);
                } else {
                    setForeground(TEXT_MUTED);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_MUTED);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // preferredSize width ~220 ensures BoxLayout X-axis sits near sidebar center (230/2)
        // so avatarLabel (56px, CENTER_ALIGNMENT) lands at x≈(115-28)=87px — properly centered.
        btn.setPreferredSize(new Dimension(220, 44));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 12));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ─── Form Row ──────────────────────────────────────────────────────

    /**
     * Stacked label-above-field form row for login/register/edit screens.
     * Uses BorderLayout so the field always stretches to fill the card width.
     */
    public static JPanel formRow(String labelText, JComponent field) {
        // BorderLayout: label sits at NORTH, field fills CENTER — guaranteed full width.
        JPanel row = new JPanel(new BorderLayout(0, 5));
        row.setOpaque(false);
        // CENTER_ALIGNMENT matches logo/button alignment in the card BoxLayout Y_AXIS,
        // and MAX width lets BoxLayout stretch this row to the card's full inner width.
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        row.add(lbl,   BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }


    // ─── Section Divider ───────────────────────────────────────────────

    /** A styled separator with a vibrant gradient (Purple to Blue). */
    public static JSeparator sectionDivider() {
        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Vibrant horizontal gradient matching the dashboard top-bars
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT_SECONDARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        sep.setForeground(BORDER_CARD);
        return sep;
    }

    // ─── Avatar Label ──────────────────────────────────────────────────

    /**
     * A circular panel containing an emoji, used as an avatar in the sidebar.
     * @param emoji  emoji character(s) to display
     * @param size   diameter of the circle in pixels
     */
    public static JLabel avatarLabel(String emoji, int size) {
        JLabel lbl = new JLabel(emoji, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Soft shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(3, 4, size - 4, size - 4);
                // Circle gradient fill
                GradientPaint gp = new GradientPaint(0, 0, BG_HOVER, 0, size, new Color(0x1A1B35));
                g2.setPaint(gp);
                g2.fillOval(0, 0, size - 1, size - 1);
                // Accent ring
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, size - 3, size - 3);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("SansSerif", Font.PLAIN, size / 2));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setPreferredSize(new Dimension(size, size));
        lbl.setMinimumSize(new Dimension(size, size));
        lbl.setMaximumSize(new Dimension(size, size));
        lbl.setOpaque(false);
        return lbl;
    }
}
