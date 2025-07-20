package main.java.edu.tue.dsvis.widgets;

import java.awt.*;
import javax.swing.*;

/**
 * Array visualisation specialised for heaps. It cooperates with
 * {@link HeapTreePanel} so that highlights stay in sync between strip and tree.
 */
public class HeapArrayStrip extends ArrayStrip {

    private HeapTreePanel twin;
    private boolean drawParent = false;
    private boolean showRank = false;

    private static final int BOX_W = 32;
    private static final int BOX_H = 32;
    private static final int PAD_Y = 4;

    public void bindTwin(HeapTreePanel t) { this.twin = t; }

    @Override
    public void flash(int idx) {
        super.flash(idx);
        if (twin != null) twin.highlight(idx);
    }

    public void setShowParent(boolean b) { drawParent = b; repaint(); }

    /** Toggle between showing raw values and rank percentages. */
    public void rankToggle() { showRank = !showRank; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!drawParent && !showRank) return; // nothing extra

        int[] data = getData();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        FontMetrics fm = g2.getFontMetrics();

        int max = 1;
        if (showRank) {
            for (int v : data) max = Math.max(max, v);
        }

        for (int i = 0; i < data.length; i++) {
            int x = i * BOX_W;
            int yTop = PAD_Y - 2;
            if (drawParent && i != 0) {
                String p = String.valueOf((i - 1) / 2);
                int tw = fm.stringWidth(p);
                g2.drawString(p, x + (BOX_W - tw) / 2, yTop);
            }
            if (showRank) {
                String rank = String.format("%d%%", (int) Math.round(100.0 * data[i] / max));
                int tw = fm.stringWidth(rank);
                g2.setColor(Color.WHITE);
                g2.fillRect(x, PAD_Y + (BOX_H/2) - fm.getAscent()/2 -1, BOX_W, fm.getAscent()+2);
                g2.setColor(Color.BLACK);
                g2.drawString(rank, x + (BOX_W - tw) / 2, PAD_Y + BOX_H/2 + fm.getAscent()/2 -2);
            }
        }
        g2.dispose();
    }
} 