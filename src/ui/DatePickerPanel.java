package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A custom calendar-style date picker panel.
 * Displays a month grid with forward/backward navigation, highlights today and
 * the currently selected date, and fires a PropertyChangeEvent when a date is picked.
 */
public class DatePickerPanel extends JPanel {

    private static final Color CAL_BG         = new Color(0x1A1C34);
    private static final Color DAY_HOVER      = new Color(0x2A2D50);
    private static final Color TODAY_RING     = new Color(0x38BDF8);
    private static final Color SELECTED_BG    = UITheme.ACCENT;
    private static final Color OTHER_MONTH_FG = new Color(0x4A4D6A);
    private static final Font  DAY_FONT       = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font  HEADER_FONT    = new Font("SansSerif", Font.BOLD,  13);
    private static final Font  DOW_FONT       = new Font("SansSerif", Font.BOLD,  11);

    private static final int CELL = 32;
    private static final int COLS = 7;
    private static final int GRID_W = COLS * CELL + (COLS - 1) * 2 + 16; // cells + gaps + padding

    private LocalDate selectedDate;
    private YearMonth displayMonth;

    private JLabel lblMonth;
    private JPanel gridPanel;

    public DatePickerPanel(LocalDate initialDate) {
        this.selectedDate = initialDate != null ? initialDate : LocalDate.now();
        this.displayMonth = YearMonth.from(selectedDate);

        setOpaque(false);
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(CAL_BG);

        // Force a fixed preferred size so JPopupMenu gives us enough room
        setPreferredSize(new Dimension(GRID_W, 280));

        add(buildHeader(), BorderLayout.NORTH);
        gridPanel = new JPanel(new GridLayout(0, COLS, 2, 2));
        gridPanel.setOpaque(false);
        add(gridPanel, BorderLayout.CENTER);

        rebuildGrid();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(CAL_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
        g2.dispose();
        super.paintComponent(g);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 2));

        JButton btnPrev = navButton("<");
        JButton btnNext = navButton(">");
        btnPrev.addActionListener(e -> { displayMonth = displayMonth.minusMonths(1); updateHeader(); rebuildGrid(); });
        btnNext.addActionListener(e -> { displayMonth = displayMonth.plusMonths(1); updateHeader(); rebuildGrid(); });

        lblMonth = new JLabel("", SwingConstants.CENTER);
        lblMonth.setFont(HEADER_FONT);
        lblMonth.setForeground(UITheme.TEXT_PRIMARY);
        updateHeader();

        header.add(btnPrev, BorderLayout.WEST);
        header.add(lblMonth, BorderLayout.CENTER);
        header.add(btnNext, BorderLayout.EAST);
        return header;
    }

    private void updateHeader() {
        String month = displayMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        lblMonth.setText(month + " " + displayMonth.getYear());
    }

    private JButton navButton(String symbol) {
        JButton btn = new JButton(symbol) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(DAY_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(UITheme.TEXT_MUTED);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(28, 26));
        btn.setMargin(new Insets(0, 0, 0, 0));
        return btn;
    }

    private void rebuildGrid() {
        gridPanel.removeAll();

        // Day-of-week headers
        String[] dow = {"S", "M", "T", "W", "T", "F", "S"};
        for (String d : dow) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(DOW_FONT);
            lbl.setForeground(UITheme.TEXT_MUTED);
            gridPanel.add(lbl);
        }

        LocalDate first = displayMonth.atDay(1);
        // Sunday = 0 offset
        int startDow = first.getDayOfWeek().getValue() % 7; // Mon=1..Sun=7, we want Sun=0
        LocalDate cursor = first.minusDays(startDow);
        LocalDate today = LocalDate.now();

        // 6 rows of 7 days
        for (int i = 0; i < 42; i++) {
            final LocalDate date = cursor;
            boolean isCurrentMonth = date.getMonth() == displayMonth.getMonth()
                                  && date.getYear() == displayMonth.getYear();
            boolean isToday    = date.equals(today);
            boolean isSelected = date.equals(selectedDate);

            JButton btn = new JButton(String.valueOf(date.getDayOfMonth())) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (isSelected) {
                        g2.setColor(SELECTED_BG);
                        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
                    } else if (getModel().isRollover() && isCurrentMonth) {
                        g2.setColor(DAY_HOVER);
                        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
                    }

                    if (isToday && !isSelected) {
                        g2.setColor(TODAY_RING);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 10, 10);
                    }

                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            btn.setFont(DAY_FONT);
            btn.setForeground(isSelected ? Color.WHITE :
                              isCurrentMonth ? UITheme.TEXT_PRIMARY : OTHER_MONTH_FG);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (isCurrentMonth) {
                btn.addActionListener(e -> {
                    LocalDate old = selectedDate;
                    selectedDate = date;
                    rebuildGrid();
                    firePropertyChange("selectedDate", old, selectedDate);
                });
            } else {
                btn.addActionListener(e -> {
                    displayMonth = YearMonth.from(date);
                    LocalDate old = selectedDate;
                    selectedDate = date;
                    updateHeader();
                    rebuildGrid();
                    firePropertyChange("selectedDate", old, selectedDate);
                });
            }

            gridPanel.add(btn);
            cursor = cursor.plusDays(1);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        LocalDate old = this.selectedDate;
        this.selectedDate = date;
        this.displayMonth = YearMonth.from(date);
        updateHeader();
        rebuildGrid();
        firePropertyChange("selectedDate", old, date);
    }

}
