package main.java.edu.tue.dsvis.widgets;

import main.java.edu.tue.dsvis.core.event.Event;
import main.java.edu.tue.dsvis.core.event.EventBus;
import main.java.edu.tue.dsvis.core.mvc.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.Arrays;

/**
 * Visualises the recursive structure of algorithms such as Merge Sort.
 * Nodes represent array slices {@code [left,right]}. SPLIT events add child
 * nodes, MERGE events mark nodes as completed (rendered in green).
 */
public class RecursionTreePanel extends JPanel implements View, EventBus.EventListener {

    private static final int NODE_W = 40;
    private static final int NODE_H = 20;
    private static final int LVL_Y = 50;

    private Node root;
    private int[] liveArray;

    // ---------------------------------------------------------------------
    // Node definition
    // ---------------------------------------------------------------------
    private static class Node {
        final int left;
        final int right;
        Node leftChild;
        Node rightChild;
        boolean completed;

        Node(int left, int right) {
            this.left = left;
            this.right = right;
        }

        boolean isLeaf() { return leftChild == null && rightChild == null; }
    }

    // ---------------------------------------------------------------------
    // Event handling
    // ---------------------------------------------------------------------

    @Override
    public void onEvent(Event e) {
        int[] idx = e.getIndices();
        if (idx.length < 2) return; // need a range
        int l = idx[0];
        int r = idx[1];
        switch (e.getType()) {
            case SPLIT -> {
                if (root == null) root = new Node(l, r);
                Node n = findNode(root, l, r);
                if (n != null && n.isLeaf()) {
                    int mid = (l + r) / 2;
                    n.leftChild = new Node(l, mid);
                    n.rightChild = new Node(mid + 1, r);
                }
            }
            case MERGE -> {
                Node n = findNode(root, l, r);
                if (n != null) n.completed = true;
            }
            default -> {}
        }
        repaint();
    }

    // Depth-first search for node by range
    private Node findNode(Node current, int l, int r) {
        if (current == null) return null;
        if (current.left == l && current.right == r) return current;
        Node n = findNode(current.leftChild, l, r);
        if (n != null) return n;
        return findNode(current.rightChild, l, r);
    }

    // ---------------------------------------------------------------------
    // Painting
    // ---------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (root == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int maxRight = root.right;
        drawNode(g2, root, 0, maxRight);
        g2.dispose();
    }

    private void drawNode(Graphics2D g2, Node n, int depth, int maxR) {
        double centerRatio = ((n.left + n.right) / 2.0) / (maxR + 1);
        int panelW = getWidth();
        int cx = (int) (centerRatio * panelW);
        int cy = LVL_Y * depth + 20;

        // Compute label first to size box
        String label;
        if (liveArray != null) {
            int[] slice = Arrays.copyOfRange(liveArray, n.left, n.right + 1);
            label = Arrays.toString(slice);
            if (label.length() > 18) label = label.substring(0, 17) + "…]";
        } else {
            label = "[" + n.left + "," + n.right + "]";
        }
        FontMetrics fm = g2.getFontMetrics();
        int nodeW = Math.max(NODE_W, fm.stringWidth(label) + 10);

        int x = cx - nodeW / 2;
        int y = cy - NODE_H / 2;

        // Draw connection lines first
        if (n.leftChild != null) {
            double childRatio = ((n.leftChild.left + n.leftChild.right) / 2.0) / (maxR + 1);
            int childCx = (int) (childRatio * panelW);
            int childCy = LVL_Y * (depth + 1) + 20;
            g2.draw(new Line2D.Double(cx, cy + NODE_H / 2, childCx, childCy - NODE_H / 2));
        }
        if (n.rightChild != null) {
            double childRatio = ((n.rightChild.left + n.rightChild.right) / 2.0) / (maxR + 1);
            int childCx = (int) (childRatio * panelW);
            int childCy = LVL_Y * (depth + 1) + 20;
            g2.draw(new Line2D.Double(cx, cy + NODE_H / 2, childCx, childCy - NODE_H / 2));
        }

        // Node rectangle
        if (n.completed) {
            g2.setColor(new Color(0xA5D6A7)); // greenish
            g2.fillRect(x, y, nodeW, NODE_H);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
        } else {
            g2.setColor(Color.WHITE);
            g2.fillRect(x, y, nodeW, NODE_H);
            g2.setColor(Color.BLACK);
        }
        g2.drawRect(x, y, nodeW, NODE_H);

        // Label
        int tw = fm.stringWidth(label);
        g2.drawString(label, cx - tw / 2, cy + fm.getAscent() / 2 - 2);

        // Recurse
        if (n.leftChild != null) drawNode(g2, n.leftChild, depth + 1, maxR);
        if (n.rightChild != null) drawNode(g2, n.rightChild, depth + 1, maxR);
    }

    // ---------------------------------------------------------------------
    // Unused View default
    // ---------------------------------------------------------------------

    @Override
    public void onReset() {
        root = null;
        repaint();
    }

    public void bindArray(int[] arr) { this.liveArray = arr; repaint(); }

    private Node findNearest(Node current, int x, int y, int depth, int maxR) {
        if (current == null) return null;
        double centerRatio = ((current.left + current.right) / 2.0) / (maxR + 1);
        int panelW = getWidth();
        int cx = (int) (centerRatio * panelW);
        int cy = LVL_Y * depth + 20;
        int x0 = cx - NODE_W / 2;
        int y0 = cy - NODE_H / 2;
        if (x >= x0 && x <= x0 + NODE_W && y >= y0 && y <= y0 + NODE_H)
            return current;
        Node n = findNearest(current.leftChild, x, y, depth + 1, maxR);
        if (n != null) return n;
        return findNearest(current.rightChild, x, y, depth + 1, maxR);
    }

    @Override
    public Dimension getPreferredSize() {
        int depth = calcDepth(root);
        return new Dimension(400, depth * LVL_Y + 40);
    }

    private int calcDepth(Node n) {
        if (n == null) return 0;
        return 1 + Math.max(calcDepth(n.leftChild), calcDepth(n.rightChild));
    }
} 