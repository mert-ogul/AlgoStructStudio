package main.java.edu.tue.dsvis.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Displays the current algorithm metric (e.g. steps, comparisons, swaps) in a
 * compact form: <name>: <value>.
 */
public class CostMeter extends JPanel {

    public static final String HEAPIFY = "Heapify steps";

    private final JLabel nameLabel = new JLabel();
    private final JLabel valueLabel = new JLabel();

    private final java.util.Map<String, Long> metrics = new java.util.HashMap<>();

    public CostMeter() {
        super(new GridLayout(1, 2, 4, 0));
        nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD));
        add(nameLabel);
        add(valueLabel);

        // defaults
        metrics.put(HEAPIFY, 0L);
        setMetric("Steps", 0);
    }

    /**
     * Updates the displayed metric.
     *
     * @param name  metric name (non-null)
     * @param value metric value
     */
    public void setMetric(String name, long value) {
        nameLabel.setText(name + ":");
        valueLabel.setText(String.valueOf(value));
    }

    /**
     * Placeholder for future support of multiple metrics.
     */
    public void addMetric(String name) {
        // TODO: implement metric selection UI (e.g., JComboBox)
    }

    /** Increment HEAPIFY metric and refresh display. */
    public void incrementHeapify(long delta) {
        long v = metrics.getOrDefault(HEAPIFY, 0L) + delta;
        metrics.put(HEAPIFY, v);
        setMetric(HEAPIFY, v);
    }
} 