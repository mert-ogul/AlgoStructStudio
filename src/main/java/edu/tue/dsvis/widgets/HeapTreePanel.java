package main.java.edu.tue.dsvis.widgets;

import main.java.edu.tue.dsvis.core.event.Event;
import main.java.edu.tue.dsvis.core.event.EventBus;
import main.java.edu.tue.dsvis.core.mvc.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 * Visualises a binary heap stored in an int[] array. Responds to {@link Event}s
 * such that array and tree stay in sync.
 */
public class HeapTreePanel extends JPanel implements View, EventBus.EventListener {

    private static final int RADIUS = 16;
    private static final int DIAM = RADIUS * 2;

    private int[] heap = new int[0];

    private final Map<Integer, Long> flashUntil = new HashMap<>();

    private boolean showIndexes = false;

    public HeapTreePanel(EventBus bus) {
        bus.register(this);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { handleHover(e.getX(), e.getY()); }
        });
    }

    /** Toggle whether node index numbers should be shown. */
    public void setShowIndexes(boolean b) { this.showIndexes = b; repaint(); }

    public void setHeap(int[] a) {
        this.heap = a;
        repaint();
    }

    public void highlight(int idx) {
        long expiry = System.nanoTime() + 200_000_000L;
        flashUntil.put(idx, expiry);
        repaint();
    }

    // simple highlight for swap path (could animate more elaborate in future)
    public void swapAnim(int i, int j) { highlight(i); highlight(j); }

    @Override
    public void onEvent(Event e) {
        switch (e.getType()) {
            case COMPARE, KEY_UPDATE -> highlight(e.getIndices()[0]);
            case SWAP -> {
                int[] idx = e.getIndices();
                if (idx.length >= 2) swapAnim(idx[0], idx[1]);
            }
            case INSERT_KEY -> highlight(e.getIndices()[0]);
            case EXTRACT_KEY -> highlight(e.getIndices()[0]);
            default -> {}
        }
    }

    private void handleHover(int x, int y) {
        int idx = locateNode(x, y);
        if (idx >= 0 && idx < heap.length) {
            EventBus.getGlobal().post(Event.highlightRange(idx, idx));
        }
    }

    private int locateNode(int x, int y) {
        for (int i = 0; i < heap.length; i++) {
            Point p = nodeCenter(i);
            int dx = x - p.x;
            int dy = y - p.y;
            if (dx * dx + dy * dy <= RADIUS * RADIUS) return i;
        }
        return -1;
    }

    private Point nodeCenter(int idx) {
        int depth = (int) (Math.log(idx + 1) / Math.log(2));
        int posInLevel = idx - ((1 << depth) - 1);
        int levelSize = 1 << depth;

        int panelW = Math.max(getWidth(), levelSize * DIAM + 40);
        int spaceX = panelW / (levelSize + 1);
        int cx = spaceX * (posInLevel + 1);
        int cy = (depth + 1) * 50;
        return new Point(cx, cy);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long now = System.nanoTime();
        flashUntil.entrySet().removeIf(e -> e.getValue() < now);

        // draw connections first
        for (int i = 0; i < heap.length; i++) {
            Point p = nodeCenter(i);
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            if (left < heap.length) {
                Point c = nodeCenter(left);
                g2.drawLine(p.x, p.y, c.x, c.y);
            }
            if (right < heap.length) {
                Point c = nodeCenter(right);
                g2.drawLine(p.x, p.y, c.x, c.y);
            }
        }

        // nodes
        for (int i = 0; i < heap.length; i++) {
            Point p = nodeCenter(i);
            boolean flash = flashUntil.containsKey(i);
            g2.setColor(flash ? new Color(0xFFEB3B) : new Color(0x2196F3));
            g2.fillOval(p.x - RADIUS, p.y - RADIUS, DIAM, DIAM);
            g2.setColor(Color.BLACK);
            g2.drawOval(p.x - RADIUS, p.y - RADIUS, DIAM, DIAM);

            String txt = String.valueOf(showIndexes ? i : heap[i]);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(txt);
            g2.drawString(txt, p.x - tw / 2, p.y + fm.getAscent() / 2 - 2);
        }

        g2.dispose();
    }

    @Override public void onReset() { flashUntil.clear(); repaint(); }
} 