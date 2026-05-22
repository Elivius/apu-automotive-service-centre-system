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
        long pending = byStatus.getOrDefault("Pending", 0L);
        long assigned = byStatus.getOrDefault("Assigned", 0L);
        long completed = byStatus.getOrDefault("Completed", 0L);
        long declined = byStatus.getOrDefault("Declined", 0L);
        long normal = all.stream().filter(apt -> "Normal".equals(apt.getServiceType())).count();
        long major = all.stream().filter(apt -> "Major".equals(apt.getServiceType())).count();
        long total = all.size();

        // ── Compute Earnings Data ──────────────────────────────────────
        Map<String, models.Payment> allPayments = services.PaymentService.getAllPaymentsMapByAppointment();
        
        long onlineCount = allPayments.values().stream()
                .filter(p -> "Paid".equals(p.getPaymentStatus()))
                .filter(p -> "Online".equals(p.getPaymentMethod()))
                .count();
        long physicalCount = allPayments.values().stream()
                .filter(p -> "Paid".equals(p.getPaymentStatus()))
                .filter(p -> "Physical".equals(p.getPaymentMethod()))
                .count();

        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        String[] monthLabels = new String[6];
        double[] monthEarnings = new double[6];
        
        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth ym = currentMonth.minusMonths(i);
            monthLabels[5 - i] = ym.format(java.time.format.DateTimeFormatter.ofPattern("MMM yy"));
            
            double totalForMonth = allPayments.values().stream()
                .filter(p -> "Paid".equals(p.getPaymentStatus()))
                .filter(p -> p.getDateTime() != null && java.time.YearMonth.from(p.getDateTime()).equals(ym))
                .mapToDouble(models.Payment::getAmount)
                .sum();
                
            monthEarnings[5 - i] = totalForMonth;
        }

        // ── Bar chart panel ──────────────────────────────────────────
        JPanel chartCard = UITheme.cardPanel();
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        ChartPanel barChart = new ChartPanel(
            new String[]{"Pending", "Assigned", "Completed", "Declined"},
            new long[]{pending, assigned, completed, declined},
            new Color[]{UITheme.WARNING, UITheme.ACCENT_SECONDARY, UITheme.SUCCESS, UITheme.DANGER},
            "Appointments by Status"
        );
        barChart.setName("barChart");
        chartCard.add(barChart, BorderLayout.CENTER);

        // ── Summary cards ────────────────────────────────────────────
        JPanel summaryRow = new JPanel(new GridLayout(1, 5, 12, 0));
        summaryRow.setOpaque(false);
        summaryRow.add(statCard("Total Appointments", total, UITheme.TEXT_PRIMARY));
        summaryRow.add(statCard("Pending",   pending,   UITheme.WARNING));
        summaryRow.add(statCard("Assigned",  assigned,  UITheme.ACCENT_SECONDARY));
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

        JPanel split = new JPanel(new GridLayout(1, 2, 16, 0));
        split.setOpaque(false);
        split.add(chartCard);
        split.add(pieCard);

        // ── Line Chart Panel ─────────────────────────────────────────
        JPanel lineCard = UITheme.cardPanel();
        lineCard.setLayout(new BorderLayout());
        lineCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        LineChartPanel lineChart = new LineChartPanel(
            monthLabels, monthEarnings, UITheme.ACCENT, "Monthly Revenue (RM)"
        );
        lineChart.setName("lineChart");
        lineCard.add(lineChart, BorderLayout.CENTER);

        // ── Payment Method Pie ───────────────────────────────────────
        JPanel paymentPieCard = UITheme.cardPanel();
        paymentPieCard.setLayout(new BorderLayout());
        paymentPieCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        PiePanel paymentPie = new PiePanel(
            new String[]{"Online", "Physical"},
            new long[]{onlineCount, physicalCount},
            new Color[]{UITheme.ACCENT_SECONDARY, UITheme.WARNING}
        );
        paymentPieCard.add(UITheme.headerLabel("Payment Methods"), BorderLayout.NORTH);
        paymentPieCard.add(paymentPie, BorderLayout.CENTER);
        paymentPieCard.setPreferredSize(new Dimension(300, 0));

        JPanel bottomRow = new JPanel(new BorderLayout(16, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(lineCard, BorderLayout.CENTER);
        bottomRow.add(paymentPieCard, BorderLayout.EAST);

        JPanel chartsContainer = new JPanel(new GridLayout(2, 1, 0, 16));
        chartsContainer.setOpaque(false);
        chartsContainer.add(split);
        chartsContainer.add(bottomRow);

        center.add(chartsContainer, BorderLayout.CENTER);

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
                g2.setFont(UITheme.FONT_BODY);
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
                g2.setFont(UITheme.FONT_BODY);
                g2.drawString(labels[i] + " (" + values[i] + ")", lx + 18, ly + 12);
            }
            g2.dispose();
        }
    }

    static class LineChartPanel extends JPanel {
        private final String[] labels;
        private final double[] values;
        private final Color color;
        private final String title;

        LineChartPanel(String[] labels, double[] values, Color color, String title) {
            this.labels = labels;
            this.values = values;
            this.color = color;
            this.title = title;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 320));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padL = 60, padR = 30, padT = 50, padB = 40;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            // Title
            g2.setColor(UITheme.TEXT_PRIMARY);
            g2.setFont(UITheme.FONT_HEADER);
            g2.drawString(title, padL, padT - 20);

            double max = -Double.MAX_VALUE;
            double min = Double.MAX_VALUE;
            for (double val : values) {
                if (val > max) max = val;
                if (val < min) min = val;
            }
            if (max == -Double.MAX_VALUE) { max = 1; min = 0; }
            if (max == min) {
                if (max == 0) max = 1;
                else {
                    min = Math.max(0, max * 0.8);
                    max = max * 1.2;
                }
            }

            double range = max - min;
            double paddedMin = Math.max(0, min - range * 0.2);
            double paddedMax = max + range * 0.2;

            int numGrids = 4;
            
            // Calculate a clean round step for the Y-axis
            double rawStep = (paddedMax - paddedMin) / numGrids;
            if (rawStep == 0) rawStep = 1;
            
            double mag = Math.pow(10, Math.floor(Math.log10(rawStep)));
            double magFrac = rawStep / mag;
            
            double cleanStep;
            if (magFrac <= 1) cleanStep = 1 * mag;
            else if (magFrac <= 1.5) cleanStep = 2 * mag; // better fit for tight ranges
            else if (magFrac <= 2) cleanStep = 2 * mag;
            else if (magFrac <= 5) cleanStep = 5 * mag;
            else cleanStep = 10 * mag;
            
            double finalMin = Math.floor(paddedMin / cleanStep) * cleanStep;
            double finalMax = finalMin + cleanStep * numGrids;

            // Draw Y-axis grid lines
            g2.setFont(UITheme.FONT_BODY);
            for (int i = 0; i <= numGrids; i++) {
                int y = padT + chartH - (i * chartH / numGrids);
                double gridVal = finalMin + (i * cleanStep);
                
                g2.setColor(UITheme.FIELD_BORDER);
                g2.drawLine(padL, y, padL + chartW, y);
                
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawString(String.format("RM%.0f", gridVal), 5, y + 4);
            }

            if (labels.length == 0) { g2.dispose(); return; }

            int[] xPoints = new int[labels.length];
            int[] yPoints = new int[labels.length];
            double graphRange = finalMax - finalMin;

            for (int i = 0; i < labels.length; i++) {
                xPoints[i] = padL + (i * chartW / Math.max(1, labels.length - 1));
                yPoints[i] = padT + chartH - (int) (chartH * (values[i] - finalMin) / graphRange);
                
                // Draw X-axis labels
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawString(labels[i], xPoints[i] - 20, padT + chartH + 25);
            }

            // Draw shaded area under line
            Polygon poly = new Polygon(xPoints, yPoints, labels.length);
            poly.addPoint(xPoints[labels.length - 1], padT + chartH);
            poly.addPoint(xPoints[0], padT + chartH);
            
            GradientPaint gp = new GradientPaint(0, padT, new Color(color.getRed(), color.getGreen(), color.getBlue(), 100), 
                                                 0, padT + chartH, new Color(color.getRed(), color.getGreen(), color.getBlue(), 10));
            g2.setPaint(gp);
            g2.fillPolygon(poly);

            // Draw line
            g2.setColor(color);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < labels.length - 1; i++) {
                g2.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
            }

            // Draw points
            for (int i = 0; i < labels.length; i++) {
                g2.setColor(UITheme.BG_CARD);
                g2.fillOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
                
                // Draw value
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.drawString(String.format("%.0f", values[i]), xPoints[i] - 15, yPoints[i] - 15);
            }

            g2.dispose();
        }
    }
}
