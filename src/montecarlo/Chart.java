package montecarlo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/** Log-log plot of RMS error against trial count, with a dashed c/sqrt(n) reference for each experiment. */
public final class Chart {
    private static final Color[] COLORS = {new Color(0x2f6fbb), new Color(0xd9534f), new Color(0x2e9e6b), new Color(0x8e5bc4)};

    private Chart() {}

    public static BufferedImage render(List<ConvergenceStudy.Row> rows, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE); g.fillRect(0, 0, w, h);

        Map<String, List<ConvergenceStudy.Row>> groups = new LinkedHashMap<>();
        for (var r : rows) groups.computeIfAbsent(r.experiment(), k -> new java.util.ArrayList<>()).add(r);

        double xMin = Double.MAX_VALUE, xMax = -Double.MAX_VALUE, yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;
        for (var r : rows) {
            double x = Math.log10(r.trials()), y = Math.log10(r.rmse());
            xMin = Math.min(xMin, x); xMax = Math.max(xMax, x); yMin = Math.min(yMin, y); yMax = Math.max(yMax, y);
        }
        yMin = Math.floor(yMin - 0.2); yMax = Math.ceil(yMax + 0.2);
        int left = 70, right = 30, top = 40, bottom = 55, pw = w - left - right, ph = h - top - bottom;
        final double fxMin = xMin, fxMax = xMax, fyMin = yMin, fyMax = yMax;
        java.util.function.DoubleUnaryOperator px = x -> left + (x - fxMin) / (fxMax - fxMin) * pw;
        java.util.function.DoubleUnaryOperator py = y -> top + (fyMax - y) / (fyMax - fyMin) * ph;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (int e = (int) xMin; e <= xMax; e++) {
            int x = (int) px.applyAsDouble(e);
            g.setColor(new Color(0xe6e6e6)); g.drawLine(x, top, x, top + ph);
            g.setColor(Color.DARK_GRAY); g.drawString("10^" + e, x - 14, top + ph + 18);
        }
        for (int e = (int) yMin; e <= yMax; e++) {
            int y = (int) py.applyAsDouble(e);
            g.setColor(new Color(0xe6e6e6)); g.drawLine(left, y, left + pw, y);
            g.setColor(Color.DARK_GRAY); g.drawString("10^" + e, 24, y + 4);
        }
        g.setColor(Color.BLACK); g.drawRect(left, top, pw, ph);
        g.drawString("Number of trials", left + pw / 2 - 40, h - 12);
        g.drawString("RMS error", 6, top - 12);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
        g.drawString("Monte Carlo error shrinks like 1/\u221An", left, 22);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 12f));

        int ci = 0, ly = top + 16;
        for (var entry : groups.entrySet()) {
            Color c = COLORS[ci++ % COLORS.length];
            double k = 0;
            for (var r : entry.getValue()) k += r.rmseTimesSqrtN();
            k /= entry.getValue().size();
            g.setColor(c);
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 5f}, 0f));
            g.drawLine((int) px.applyAsDouble(xMin), (int) py.applyAsDouble(Math.log10(k) - 0.5 * xMin),
                       (int) px.applyAsDouble(xMax), (int) py.applyAsDouble(Math.log10(k) - 0.5 * xMax));
            g.setStroke(new BasicStroke(2f));
            int[] xs = new int[entry.getValue().size()], ys = new int[xs.length];
            for (int i = 0; i < xs.length; i++) {
                var r = entry.getValue().get(i);
                xs[i] = (int) px.applyAsDouble(Math.log10(r.trials()));
                ys[i] = (int) py.applyAsDouble(Math.log10(r.rmse()));
            }
            g.drawPolyline(xs, ys, xs.length);
            for (int i = 0; i < xs.length; i++) g.fillOval(xs[i] - 3, ys[i] - 3, 7, 7);
            g.fillRect(left + pw - 170, ly - 9, 12, 12);
            g.setColor(Color.BLACK); g.drawString(entry.getKey(), left + pw - 152, ly + 2);
            ly += 18;
        }
        g.setColor(Color.GRAY); g.drawString("dashed: c/\u221An reference", left + pw - 170, ly + 2);
        g.dispose();
        return img;
    }

    public static void show(BufferedImage img) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Monte Carlo convergence");
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.add(new JLabel(new ImageIcon(img)));
            f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
        });
    }
}
