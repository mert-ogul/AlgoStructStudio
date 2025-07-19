package main.java.edu.tue.dsvis.widgets;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Visualises an {@code int[]} as equal-width boxes laid out horizontally.
 * <p>
 * Helper methods such as {@link #flash(int)} and {@link #swap(int, int)} allow
 * simple animations that can be driven by an external {@code Timeline}.
 * </p>
 */
public class ArrayStrip extends JPanel {

    private static final int BOX_W = 32;
    private static final int BOX_H = 32;
    private static final int PAD_Y = 4;

    // Base and highlight colours
    private static final Color BASE      = new Color(0x2196F3); // blue
    private static final Color FLASH     = new Color(0xFFEB3B); // amber
    private static final Color SWAP_A    = new Color(0x4CAF50); // green
    private static final Color SWAP_B    = new Color(0xF44336); // red

    private int[] data = new int[0];

    // Map of index -> expiry time in nanos for flash effect
    private final Map<Integer, Long> flashUntil = new HashMap<>();

    public ArrayStrip() {
        // Placeholder listener for future drag-and-drop reordering support.
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {/* TODO */}
        });
    }

    // Public API

    public void setData(int[] arr) {
        this.data = Arrays.copyOf(arr, arr.length);
        revalidate();
        repaint();
    }

    /**
     * Briefly highlights the given index using a flash colour; effect lasts
     * ~150 ms.
     */
    public void flash(int index) {
        long expiry = System.nanoTime() + 150_000_000L; // 150 ms
        flashUntil.put(index, expiry);
        repaint();

        // Schedule a repaint after expiry to clear highlight.
        new Timer(160, e -> {
            ((Timer) e.getSource()).stop();
            repaint();
        }).start();
    }

    /**
     * Swaps the values at indices {@code i} and {@code j} and repaints.
     */
    public void swap(int i, int j) {
        if (i < 0 || j < 0 || i >= data.length || j >= data.length) return;
        int tmp = data[i];
        data[i] = data[j];
        data[j] = tmp;
        repaint();
    }

    /**
     * Shifts element at {@code src} to {@code dst}, moving intermediate
     * elements accordingly.
     */
    public void shift(int src, int dst) {
        if (src < 0 || dst < 0 || src >= data.length || dst >= data.length) return;
        if (src == dst) return;
        int val = data[src];
        if (src < dst) {
            System.arraycopy(data, src + 1, data, src, dst - src);
        } else {
            System.arraycopy(data, dst, data, dst + 1, src - dst);
        }
        data[dst] = val;
        repaint();
    }

    // Swing overrides

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(data.length * BOX_W, BOX_H + PAD_Y * 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        long now = System.nanoTime();
        // Clean up expired flash entries
        Iterator<Map.Entry<Integer, Long>> it = flashUntil.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Long> entry = it.next();
            if (entry.getValue() < now) {
                it.remove();
            }
        }

        FontMetrics fm = g2.getFontMetrics();
        int baseline = PAD_Y + (BOX_H + fm.getAscent()) / 2 - 2; // simple vertical centering

        for (int i = 0; i < data.length; i++) {
            int x = i * BOX_W;
            int y = PAD_Y;

            // Determine colour
            Color fill = BASE;
            if (flashUntil.containsKey(i)) {
                fill = FLASH;
            }
            g2.setColor(fill);
            g2.fillRect(x, y, BOX_W, BOX_H);

            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, BOX_W, BOX_H);

            // Draw value
            String text = String.valueOf(data[i]);
            int tw = fm.stringWidth(text);
            g2.drawString(text, x + (BOX_W - tw) / 2, baseline);
        }

        g2.dispose();
    }
} 