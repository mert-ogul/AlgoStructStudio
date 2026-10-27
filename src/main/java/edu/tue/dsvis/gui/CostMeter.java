package edu.tue.dsvis.gui;

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
        super(new FlowLayout(FlowLayout.LEFT, 4, 0));
        nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD));
        add(nameLabel);
        add(valueLabel);

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
}
