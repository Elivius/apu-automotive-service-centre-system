package ui.manager;

import models.Appointment;
import services.AppointmentService;
import ui.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analytics panel for Managers.
 * Draws a bar chart using Graphics2D showing appointment counts by status
 * and a side panel for Normal vs Major service breakdown.
 */
public class ReportsPanel extends JPanel {

    public ReportsPanel() {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.titleLabel("Analyse Reports"), BorderLayout.WEST);

        JButton btnRefresh = UITheme.secondaryButton("↻ Refresh");
        btnRefresh.setName("btnRefresh");
        btnRefresh.addActionListener(e -> { removeAll(); buildUI(); revalidate(); repaint(); });
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        List<Appointment> all = AppointmentService.getAllAppointments();

        // Compute stats
        Map<String, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(apt -> normaliseStatus(apt.getStatus()), Collectors.counting()));
        long pending   = byStatus.getOrDefault("Pending",   0L);
        long assigned  = byStatus.getOrDefault("Assigned",  0L);
        long completed = byStatus.getOrDefault("Completed", 0L);
        long declined  = byStatus.getOrDefault("Declined",  0L);
        long normal    = all.stream().filter(apt -> "Normal".equals(apt.getServiceType())).count();
        long major     = all.stream().filter(apt -> "Major".equals(apt.getServiceType())).count();
        long total     = all.size();

        // ── Bar chart panel ──────────────────────────────────────────
        JPanel chartCard = UITheme.cardPanel();
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        ChartPanel barChart = new ChartPanel(
            new String[]{"Pending", "Assigned", "Completed", "Declined"},
            new long[]{pending, assigned, completed, declined},
            new Color[]{UITheme.WARNING, UITheme.BG_SIDEBAR, UITheme.SUCCESS, UITheme.DANGER},
            "Appointments by Status"
        );
        barChart.setName("barChart");
        chartCard.add(barChart, BorderLayout.CENTER);

        // ── Summary cards ────────────────────────────────────────────
        JPanel summaryRow = new JPanel(new GridLayout(1, 5, 12, 0));
        summaryRow.setOpaque(false);
        summaryRow.add(statCard("Total Appointments", total, UITheme.TEXT_PRIMARY));
        summaryRow.add(statCard("Pending",   pending,   UITheme.WARNING));
        summaryRow.add(statCard("Assigned",  assigned,  UITheme.BG_SIDEBAR));
        summaryRow.add(statCard("Completed", completed, UITheme.SUCCESS));
        summaryRow.add(statCard("Declined",  declined,  UITheme.DANGER));

        // ── Service type pie ──────────────────────────────────────────
        JPanel pieCard = UITheme.cardPanel();
        pieCard.setLayout(new BorderLayout());
        pieCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        PiePanel pie = new PiePanel(
            new String[]{"Normal", "Major"},
            new long[]{normal, major},
            new Color[]{UITheme.SUCCESS, UITheme.ACCENT}
        );
        pieCard.add(UITheme.headerLabel("Service Type Breakdown"), BorderLayout.NORTH);
        pieCard.add(pie, BorderLayout.CENTER);

        // ── Layout ─────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(summaryRow, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartCard, pieCard);
        split.setDividerLocation(600);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(UITheme.BG_DARK);
        center.add(split, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private String normaliseStatus(String status) {
        if (status == null) return "Unknown";
        if (status.startsWith("Assigned")) return "Assigned";
        return status;
    }

    private JPanel statCard(String label, long value, Color valueColor) {
        JPanel card = UITheme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel num = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        num.setFont(new Font("SansSerif", Font.BOLD, 28));
        num.setForeground(valueColor);
        num.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lbl = UITheme.mutedLabel(label);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(num);
        card.add(Box.createVerticalStrut(4));
        card.add(lbl);
        return card;
    }

    // ── Inner classes for charts ──────────────────────────────────────
    static class ChartPanel extends JPanel {
        private final String[] labels;
        private final long[]   values;
        private final Color[]  colours;
        private final String   title;

        ChartPanel(String[] labels, long[] values, Color[] colours, String title) {
            this.labels = labels;
            this.values = values;
            this.colours = colours;
            this.title = title;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 320));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padL = 50, padR = 20, padT = 40, padB = 60;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            // Title
            g2.setColor(UITheme.TEXT_PRIMARY);
            g2.setFont(UITheme.FONT_HEADER);
            g2.drawString(title, padL, padT - 12);

            long max = 1;
            for (long val : values) if (val > max) max = val;

            int barW = chartW / (labels.length * 2);

            for (int i = 0; i < labels.length; i++) {
                int barH = (int) (chartH * values[i] / (double) max);
                int x = padL + i * (chartW / labels.length) + barW / 2;
                int y = padT + chartH - barH;

                // Bar
                g2.setColor(colours[i]);
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                // Value on top
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.setFont(UITheme.FONT_BODY);
                g2.drawString(String.valueOf(values[i]), x + barW / 2 - 6, y - 4);

                // Label at bottom
                g2.setFont(UITheme.FONT_SMALL);
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawString(labels[i], x, padT + chartH + 18);
            }

            // Y-axis line
            g2.setColor(UITheme.FIELD_BORDER);
            g2.drawLine(padL, padT, padL, padT + chartH);
            g2.drawLine(padL, padT + chartH, padL + chartW, padT + chartH);

            g2.dispose();
        }
    }

    static class PiePanel extends JPanel {
        private final String[] labels;
        private final long[]   values;
        private final Color[]  colours;

        PiePanel(String[] labels, long[] values, Color[] colours) {
            this.labels = labels;
            this.values = values;
            this.colours = colours;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int size = Math.min(w, h) - 60;
            int x = (w - size) / 2, y = (h - size) / 2 - 20;

            long total = 0; for (long val : values) total += val;
            if (total == 0) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(UITheme.FONT_BODY);
                g2.drawString("No data", w/2 - 25, h/2);
                g2.dispose();
                return;
            }

            double startAngle = 90;
            for (int i = 0; i < values.length; i++) {
                double angle = 360.0 * values[i] / total;
                g2.setColor(colours[i]);
                g2.fillArc(x, y, size, size, (int) startAngle, -(int) angle);
                startAngle -= angle;
            }

            // Legend
            int ly = y + size + 16;
            for (int i = 0; i < labels.length; i++) {
                int lx = w / 2 - (labels.length * 80) / 2 + i * 90;
                g2.setColor(colours[i]);
                g2.fillRoundRect(lx, ly, 14, 14, 4, 4);
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.setFont(UITheme.FONT_SMALL);
                g2.drawString(labels[i] + " (" + values[i] + ")", lx + 18, ly + 12);
            }
            g2.dispose();
        }
    }
}
