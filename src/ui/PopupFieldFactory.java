package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;

/**
 * Class for creating styled popup input fields (Date, Time, Dropdown)
 */
public class PopupFieldFactory {

    private static final Color CAL_BG         = new Color(0x1A1C34);
    private static final Color DAY_HOVER      = new Color(0x2A2D50);
    private static final Color SELECTED_BG    = UITheme.ACCENT;
    private static final Font  DAY_FONT       = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font  HEADER_FONT    = new Font("SansSerif", Font.BOLD,  13);

    // ─── Static helper: create a date-picker trigger field ──────────────

    /**
     * Creates a styled date field (JTextField + calendar button) that pops up
     * a DatePickerPanel in a lightweight popup when clicked.
     *
     * @param initial  initial date (null -> today)
     * @param holder   a single-element array that will always hold the current selection
     * @return a JPanel wrapper component to embed in a form
     */
    public static JPanel createDateField(LocalDate initial, LocalDate[] holder) {
        LocalDate init = initial != null ? initial : LocalDate.now();
        holder[0] = init;

        JTextField tf = new JTextField(init.toString(), 12);
        tf.setEditable(false);
        tf.setBackground(UITheme.FIELD_BG);
        tf.setForeground(UITheme.TEXT_PRIMARY);
        tf.setCaretColor(UITheme.ACCENT);
        tf.setFont(UITheme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.FIELD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        tf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Calendar trigger button 
        JButton btnCal = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Hover background
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0x2A2D50));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                // Draw a mini calendar icon
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int iw = 16, ih = 14;
                int ix = cx - iw / 2;
                int iy = cy - ih / 2;

                g2.setColor(UITheme.ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                // Calendar body
                g2.drawRoundRect(ix, iy + 2, iw, ih - 2, 3, 3);
                // Top bar
                g2.fillRect(ix, iy + 2, iw + 1, 5);
                // Two "pins" on top
                g2.drawLine(ix + 4, iy, ix + 4, iy + 4);
                g2.drawLine(ix + iw - 4, iy, ix + iw - 4, iy + 4);
                // Grid dots
                g2.setColor(CAL_BG);
                int dotSize = 2;
                g2.fillRect(ix + 4, iy + 9, dotSize, dotSize);
                g2.fillRect(ix + 8, iy + 9, dotSize, dotSize);
                g2.fillRect(ix + 12, iy + 9, dotSize, dotSize);
                g2.fillRect(ix + 4, iy + 12, dotSize, dotSize);
                g2.fillRect(ix + 8, iy + 12, dotSize, dotSize);

                g2.dispose();
            }
        };
        btnCal.setOpaque(false);
        btnCal.setContentAreaFilled(false);
        btnCal.setBorderPainted(false);
        btnCal.setFocusPainted(false);
        btnCal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCal.setPreferredSize(new Dimension(34, 34));
        btnCal.setToolTipText("Pick a date");

        JPanel wrapper = new JPanel(new BorderLayout(4, 0));
        wrapper.setOpaque(false);
        wrapper.add(tf, BorderLayout.CENTER);
        wrapper.add(btnCal, BorderLayout.EAST);

        // Popup behaviour
        Runnable showPopup = () -> {
            DatePickerPanel picker = new DatePickerPanel(holder[0]);

            JPopupMenu popup = new JPopupMenu();
            popup.setLayout(new BorderLayout());
            popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_CARD, 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            popup.setBackground(CAL_BG);
            popup.add(picker, BorderLayout.CENTER);

            // Add "Today" shortcut button
            JButton btnToday = new JButton("Today") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(new Color(0x2A2D50));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnToday.setFont(UITheme.FONT_BODY);
            btnToday.setForeground(UITheme.ACCENT_SECONDARY);
            btnToday.setOpaque(false);
            btnToday.setContentAreaFilled(false);
            btnToday.setBorderPainted(false);
            btnToday.setFocusPainted(false);
            btnToday.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnToday.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));
            btnToday.addActionListener(ev -> picker.setSelectedDate(LocalDate.now()));

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            footer.setOpaque(false);
            footer.add(btnToday);
            popup.add(footer, BorderLayout.SOUTH);

            picker.addPropertyChangeListener("selectedDate", evt -> {
                LocalDate chosen = (LocalDate) evt.getNewValue();
                holder[0] = chosen;
                tf.setText(chosen.toString());
                popup.setVisible(false);
            });

            popup.show(wrapper, 0, wrapper.getHeight() + 2);
        };

        btnCal.addActionListener(e -> showPopup.run());
        tf.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showPopup.run(); }
        });

        return wrapper;
    }

    // ─── Static helper: create a time-picker trigger field ──────────────

    /**
     * Creates a styled time field (JTextField + clock button) that pops up
     * a scrollable time picker when clicked. Uses the same 30-min business-hour
     * slots (08:00–21:30) as the rest of the application.
     *
     * @param initialSlot  initial time string like "08:00" (null -> "08:00")
     * @param holder       a single-element array that will always hold the current "HH:mm" selection
     * @return a JPanel wrapper component to embed in a form
     */
    public static JPanel createTimeField(String initialSlot, String[] holder) {
        String init = initialSlot != null ? initialSlot : "08:00";
        holder[0] = init;

        JTextField tf = new JTextField(init, 8);
        tf.setEditable(false);
        tf.setBackground(UITheme.FIELD_BG);
        tf.setForeground(UITheme.TEXT_PRIMARY);
        tf.setCaretColor(UITheme.ACCENT);
        tf.setFont(UITheme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.FIELD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        tf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Clock trigger button — custom-painted icon
        JButton btnClock = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(new Color(0x2A2D50));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                // Draw a mini clock icon
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int r = 8;

                g2.setColor(UITheme.ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                // Hour hand (pointing to ~2 o'clock)
                g2.drawLine(cx, cy, cx + 3, cy - 5);
                // Minute hand (pointing to ~12 o'clock)
                g2.drawLine(cx, cy, cx - 1, cy - 7);
                // Center dot
                g2.fillOval(cx - 1, cy - 1, 3, 3);

                g2.dispose();
            }
        };
        btnClock.setOpaque(false);
        btnClock.setContentAreaFilled(false);
        btnClock.setBorderPainted(false);
        btnClock.setFocusPainted(false);
        btnClock.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClock.setPreferredSize(new Dimension(34, 34));
        btnClock.setToolTipText("Pick a time");

        JPanel wrapper = new JPanel(new BorderLayout(4, 0));
        wrapper.setOpaque(false);
        wrapper.add(tf, BorderLayout.CENTER);
        wrapper.add(btnClock, BorderLayout.EAST);

        // Build the 30-minute time slots
        String[] slots = new String[28];
        int si = 0;
        for (int h = 8; h < 22; h++) {
            slots[si++] = String.format("%02d:00", h);
            slots[si++] = String.format("%02d:30", h);
        }

        // Popup behaviour
        Runnable showPopup = () -> {
            JPopupMenu popup = new JPopupMenu();
            popup.setLayout(new BorderLayout());
            popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_CARD, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
            ));
            popup.setBackground(CAL_BG);

            // Title
            JLabel lblTitle = new JLabel("Select Time", SwingConstants.CENTER);
            lblTitle.setFont(HEADER_FONT);
            lblTitle.setForeground(UITheme.TEXT_PRIMARY);
            lblTitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 8, 0));
            popup.add(lblTitle, BorderLayout.NORTH);

            // Scrollable time slot grid: 2 columns x 14 rows
            JPanel grid = new JPanel(new GridLayout(0, 2, 6, 4));
            grid.setOpaque(false);

            for (String slot : slots) {
                boolean isSelected = slot.equals(holder[0]);
                JButton btn = new JButton(slot) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        if (isSelected) {
                            g2.setColor(SELECTED_BG);
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        } else if (getModel().isRollover()) {
                            g2.setColor(DAY_HOVER);
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        }

                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                btn.setFont(DAY_FONT);
                btn.setForeground(isSelected ? Color.WHITE : UITheme.TEXT_PRIMARY);
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setPreferredSize(new Dimension(68, 30));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                btn.addActionListener(ev -> {
                    holder[0] = slot;
                    tf.setText(slot);
                    popup.setVisible(false);
                });

                grid.add(btn);
            }

            JScrollPane sp = new JScrollPane(grid);
            sp.setBorder(null);
            sp.getViewport().setBackground(CAL_BG);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            sp.setPreferredSize(new Dimension(160, 240));

            // Scroll to the selected time slot
            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i].equals(holder[0])) {
                        Component c = grid.getComponent(i);
                        grid.scrollRectToVisible(c.getBounds());
                        break;
                    }
                }
            });

            popup.add(sp, BorderLayout.CENTER);
            popup.show(wrapper, 0, wrapper.getHeight() + 2);
        };

        btnClock.addActionListener(e -> showPopup.run());
        tf.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showPopup.run(); }
        });

        return wrapper;
    }

    // ─── Static helper: create a styled dropdown field ──────────────────

    /**
     * Creates a styled dropdown field (JTextField + chevron button) that pops up
     * a list of options when clicked. Matches the date/time picker visual style.
     *
     * @param options  array of option strings
     * @param holder   a single-element array that will always hold the current selection
     * @param onChange optional callback fired when selection changes (may be null)
     * @return a JPanel wrapper component to embed in a form
     */
    public static JPanel createDropdownField(String[] options, String[] holder, Runnable onChange) {
        holder[0] = options.length > 0 ? options[0] : "";

        JTextField tf = new JTextField(holder[0], 12);
        tf.setEditable(false);
        tf.setBackground(UITheme.FIELD_BG);
        tf.setForeground(UITheme.TEXT_PRIMARY);
        tf.setCaretColor(UITheme.ACCENT);
        tf.setFont(UITheme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.FIELD_BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        tf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Chevron button — custom-painted down arrow
        JButton btnChevron = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(new Color(0x2A2D50));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                // Draw a chevron/down arrow
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(UITheme.ACCENT);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 5, cy - 2, cx, cy + 3);
                g2.drawLine(cx, cy + 3, cx + 5, cy - 2);

                g2.dispose();
            }
        };
        btnChevron.setOpaque(false);
        btnChevron.setContentAreaFilled(false);
        btnChevron.setBorderPainted(false);
        btnChevron.setFocusPainted(false);
        btnChevron.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnChevron.setPreferredSize(new Dimension(34, 34));

        JPanel wrapper = new JPanel(new BorderLayout(4, 0));
        wrapper.setOpaque(false);
        wrapper.add(tf, BorderLayout.CENTER);
        wrapper.add(btnChevron, BorderLayout.EAST);

        // Popup behaviour
        Runnable showPopup = () -> {
            JPopupMenu popup = new JPopupMenu();
            popup.setLayout(new BorderLayout());
            popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_CARD, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
            popup.setBackground(CAL_BG);

            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setOpaque(false);

            for (String option : options) {
                boolean isSelected = option.equals(holder[0]);
                JButton btn = new JButton(option) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        if (isSelected) {
                            g2.setColor(SELECTED_BG);
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        } else if (getModel().isRollover()) {
                            g2.setColor(DAY_HOVER);
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        }

                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                btn.setFont(DAY_FONT);
                btn.setForeground(isSelected ? Color.WHITE : UITheme.TEXT_PRIMARY);
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setMargin(new Insets(0, 8, 0, 8));
                btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
                btn.setPreferredSize(new Dimension(wrapper.getWidth() - 12, 32));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                btn.addActionListener(ev -> {
                    holder[0] = option;
                    tf.setText(option);
                    popup.setVisible(false);
                    if (onChange != null) onChange.run();
                });

                list.add(btn);
            }

            JScrollPane sp = new JScrollPane(list);
            sp.setBorder(null);
            sp.getViewport().setBackground(CAL_BG);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            // Size the popup to fit the options, capped at 8 visible items
            int visibleItems = Math.min(options.length, 8);
            sp.setPreferredSize(new Dimension(
                Math.max(wrapper.getWidth(), 140),
                visibleItems * 34 + 8
            ));

            popup.add(sp, BorderLayout.CENTER);
            popup.show(wrapper, 0, wrapper.getHeight() + 2);
        };

        btnChevron.addActionListener(e -> showPopup.run());
        tf.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showPopup.run(); }
        });

        return wrapper;
    }
}
