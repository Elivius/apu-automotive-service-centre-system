package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Centralised design system for the APU-ASC application.
 * Provides colours, fonts, and helper factory methods so
 * every screen looks consistent without repeating style code.
 */
public class UITheme {

    // ─── Colour Palette ────────────────────────────────────────────────
    public static final Color BG_DARK       = new Color(0x1A1A2E);
    public static final Color BG_CARD       = new Color(0x16213E);
    public static final Color BG_SIDEBAR    = new Color(0x0F3460);
    public static final Color ACCENT        = new Color(0xE94560);
    public static final Color ACCENT_HOVER  = new Color(0xFF6B6B);
    public static final Color TEXT_PRIMARY  = new Color(0xEAEAEA);
    public static final Color TEXT_MUTED    = new Color(0x9CA3AF);
    public static final Color SUCCESS       = new Color(0x10B981);
    public static final Color WARNING       = new Color(0xF59E0B);
    public static final Color DANGER        = new Color(0xEF4444);
    public static final Color TABLE_HEADER  = new Color(0x0F3460);
    public static final Color TABLE_ALT_ROW = new Color(0x1E2A4A);
    public static final Color FIELD_BG      = new Color(0x0D1B3E);
    public static final Color FIELD_BORDER  = new Color(0x2D3B6E);

    // ─── Fonts ─────────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD,  22);
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD,  15);
    public static final Font FONT_BODY   = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD,  13);

    // ─── Global Defaults ───────────────────────────────────────────────
    public static void applyGlobalDefaults() {
        UIManager.put("OptionPane.background",            BG_CARD);
        UIManager.put("Panel.background",                 BG_DARK);
        UIManager.put("OptionPane.messageForeground",     TEXT_PRIMARY);
        UIManager.put("Button.background",                ACCENT);
        UIManager.put("Button.foreground",                Color.WHITE);
        UIManager.put("Button.font",                      FONT_BUTTON);
        UIManager.put("Label.foreground",                 TEXT_PRIMARY);
        UIManager.put("Label.font",                       FONT_BODY);
        UIManager.put("TextField.background",             FIELD_BG);
        UIManager.put("TextField.foreground",             TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",        TEXT_PRIMARY);
        UIManager.put("PasswordField.background",         FIELD_BG);
        UIManager.put("PasswordField.foreground",         TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground",    TEXT_PRIMARY);
        UIManager.put("ComboBox.background",              FIELD_BG);
        UIManager.put("ComboBox.foreground",              TEXT_PRIMARY);
        UIManager.put("TextArea.background",              FIELD_BG);
        UIManager.put("TextArea.foreground",              TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground",         TEXT_PRIMARY);
        UIManager.put("ScrollPane.background",            BG_DARK);
        UIManager.put("Viewport.background",              BG_DARK);
        UIManager.put("Table.background",                 BG_CARD);
        UIManager.put("Table.foreground",                 TEXT_PRIMARY);
        UIManager.put("Table.gridColor",                  FIELD_BORDER);
        UIManager.put("Table.selectionBackground",        ACCENT);
        UIManager.put("Table.selectionForeground",        Color.WHITE);
        UIManager.put("TableHeader.background",           TABLE_HEADER);
        UIManager.put("TableHeader.foreground",           Color.WHITE);
        UIManager.put("TableHeader.font",                 FONT_HEADER);
        UIManager.put("TabbedPane.background",            BG_CARD);
        UIManager.put("TabbedPane.foreground",            TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected",              BG_SIDEBAR);
        UIManager.put("Spinner.background",               FIELD_BG);
        UIManager.put("Spinner.foreground",               TEXT_PRIMARY);
    }

    // ─── Factory Methods ───────────────────────────────────────────────

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

    /** Standard accent-coloured rounded button. */
    public static JButton accentButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
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
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, 38));
        return btn;
    }

    /** A subtler secondary button (outline style). */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? new Color(0x2D3B6E) : BG_CARD;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(FIELD_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_PRIMARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 24, 34));
        return btn;
    }

    /** Danger (red) button for destructive actions. */
    public static JButton dangerButton(String text) {
        JButton btn = accentButton(text);
        btn.putClientProperty("dangerBtn", true);
        // Override paint to use DANGER colour
        btn.setForeground(Color.WHITE);
        return new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xFF6666) : DANGER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
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
    }

    /** A styled JTextField. */
    public static JTextField styledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setBackground(FIELD_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    /** A styled JPasswordField. */
    public static JPasswordField styledPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setBackground(FIELD_BG);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(TEXT_PRIMARY);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return pf;
    }

    /** A styled JTextArea wrapped in a JScrollPane. */
    public static JScrollPane styledTextArea(JTextArea ta) {
        ta.setBackground(FIELD_BG);
        ta.setForeground(TEXT_PRIMARY);
        ta.setCaretColor(TEXT_PRIMARY);
        ta.setFont(FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(BorderFactory.createLineBorder(FIELD_BORDER, 1));
        sp.getViewport().setBackground(FIELD_BG);
        return sp;
    }

    /** A card-style panel (rounded dark surface). */
    public static JPanel cardPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    /** Status badge label (colour-coded). */
    public static JLabel statusBadge(String status) {
        JLabel lbl = new JLabel(" " + status + " ");
        lbl.setFont(FONT_SMALL);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        if (status == null) { 
            lbl.setBackground(TEXT_MUTED); 
            lbl.setForeground(Color.WHITE); 
            return lbl; 
        }
        switch (status) {
            case "Completed": 
                lbl.setBackground(SUCCESS);  
                lbl.setForeground(Color.WHITE); 
                break;
            case "Pending":   
                lbl.setBackground(WARNING);  
                lbl.setForeground(Color.BLACK); 
                break;
            case "Declined":  
                lbl.setBackground(DANGER);   
                lbl.setForeground(Color.WHITE); 
                break;
            default:          
                lbl.setBackground(BG_SIDEBAR); 
                lbl.setForeground(Color.WHITE); 
                break;
        }
        return lbl;
    }

    /** A styled JScrollPane wrapping a JTable. */
    public static JScrollPane styledTable(JTable table) {
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(FIELD_BORDER);
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_CARD);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(FONT_HEADER);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(FIELD_BORDER, 1));
        return sp;
    }

    /** An input row for a form: label + component side by side. */
    public static JPanel formRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(140, 30));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }
}
