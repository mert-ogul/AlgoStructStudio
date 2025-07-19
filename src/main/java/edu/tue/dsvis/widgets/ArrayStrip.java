package main.java.edu.tue.dsvis.widgets;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
    private static final int INDEX_H = 12;
    private static final int PAD_Y = 4;

    // Base and highlight colours
    private static final Color BASE      = new Color(0x2196F3); // blue
    private static final Color FLASH     = new Color(0xFFEB3B); // amber
    private static final Color SWAP_A    = new Color(0x4CAF50); // green
    private static final Color SWAP_B    = new Color(0xF44336); // red
    private static final Color DIMMED    = new Color(0xB0BEC5); // grey for outside window

    private int[] data = new int[0];

    // Active window for search highlighting; when low>high window is disabled
    private int windowLow = -1;
    private int windowHigh = -1;

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

    /** Flashes all indices in [l,r] for 200ms. */
    public void flashRange(int l, int r) {
        if (l < 0) l = 0;
        if (r >= data.length) r = data.length - 1;
        long expiry = System.nanoTime() + 200_000_000L;
        for (int i = l; i <= r; i++) {
            flashUntil.put(i, expiry);
        }
        repaint();
    }

    /** Clears all flash highlights. */
    public void clearHighlights() {
        flashUntil.clear();
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

    /** Returns a defensive copy of the current data. */
    public int[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /** Sets the value at the specified index and repaints. */
    public void setValue(int index, int value) {
        if (index < 0 || index >= data.length) return;
        data[index] = value;
        repaint();
    }

    /** Sets current active window for search visualisation. */
    public void setSearchWindow(int low, int high) {
        this.windowLow = low;
        this.windowHigh = high;
        repaint();
    }

    /** Clears any active search window. */
    public void clearSearchWindow() {
        this.windowLow = -1;
        this.windowHigh = -1;
        repaint();
    }

    // Swing overrides

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(data.length * BOX_W, BOX_H + PAD_Y * 2 + INDEX_H);
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
        int baseline = PAD_Y + (BOX_H + fm.getAscent()) / 2 - 2; // value text baseline

        int indexBaseline = PAD_Y + BOX_H + INDEX_H - 2;

        for (int i = 0; i < data.length; i++) {
            int x = i * BOX_W;
            int y = PAD_Y;

            // Determine colour
            Color fill = BASE;
            if (windowLow >= 0 && windowHigh >= windowLow) {
                if (i < windowLow || i > windowHigh) {
                    fill = DIMMED;
                }
            }
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

            // Draw index number below box in smaller font
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 9f));
            String idxStr = String.valueOf(i);
            int iw = g2.getFontMetrics().stringWidth(idxStr);
            g2.drawString(idxStr, x + (BOX_W - iw) / 2, indexBaseline);
            g2.setFont(fm.getFont());
        }

        g2.dispose();
    }
} 