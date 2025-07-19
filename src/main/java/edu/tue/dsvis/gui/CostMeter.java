package main.java.edu.tue.dsvis.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Displays the current algorithm metric (e.g. steps, comparisons, swaps) in a
 * compact form: <name>: <value>.
 */
public class CostMeter extends JPanel {

    private final JLabel nameLabel = new JLabel();
    private final JLabel valueLabel = new JLabel();

    public CostMeter() {
        super(new GridLayout(1, 2, 4, 0));
        nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD));
        add(nameLabel);
        add(valueLabel);

        // defaults
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
} 