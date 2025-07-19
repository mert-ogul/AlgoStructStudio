package main.java.edu.tue.dsvis.modules.arrays.view;

import main.java.edu.tue.dsvis.core.event.Event;
import main.java.edu.tue.dsvis.core.event.EventBus;
import main.java.edu.tue.dsvis.core.mvc.View;
import main.java.edu.tue.dsvis.gui.CostMeter;
import main.java.edu.tue.dsvis.gui.PseudocodePane;
import main.java.edu.tue.dsvis.widgets.ArrayStrip;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Comprehensive view for array algorithms. Combines array visualisation,
 * pseudocode display, and user controls.
 */
public class ArraysView implements View, EventBus.EventListener {

    private final EventBus bus;

    private final JPanel root = new JPanel(new BorderLayout(8, 8));
    private final ArrayStrip strip = new ArrayStrip();
    private final PseudocodePane pseudocode = new PseudocodePane("/pseudocode/linear_search.txt");
    private final CostMeter costMeter = new CostMeter();

    // Simple input bar components
    private final JTextField arrayField = new JTextField(20);
    private final JTextField targetField = new JTextField(5);
    private final JComboBox<String> algoBox = new JComboBox<>();
    private final JButton runButton = new JButton("Run");

    private long steps = 0;  // cost metric counter

    public ArraysView(EventBus bus) {
        this.bus = bus;
        buildUI();
        bus.register(this);
    }

    private void buildUI() {
        // NORTH input bar
        JPanel inputBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputBar.add(new JLabel("Array:"));
        inputBar.add(arrayField);
        inputBar.add(new JLabel("Target:"));
        inputBar.add(targetField);
        inputBar.add(algoBox);
        inputBar.add(runButton);
        root.add(inputBar, BorderLayout.NORTH);

        // CENTER strip inside scroll pane
        root.add(new JScrollPane(strip), BorderLayout.CENTER);

        // EAST pseudocode
        root.add(pseudocode, BorderLayout.EAST);

        // SOUTH cost meter
        root.add(costMeter, BorderLayout.SOUTH);
    }

    // Public API

    public JPanel getRoot() {
        return root;
    }

    public String getArrayInput() {
        return arrayField.getText();
    }

    public String getTargetInput() {
        return targetField.getText();
    }

    public String getSelectedAlgorithm() {
        Object sel = algoBox.getSelectedItem();
        return sel == null ? "" : sel.toString();
    }

    public void setArrayInput(String text) { arrayField.setText(text); }
    public void setTargetInput(String text) { targetField.setText(text); }

    public void addRunListener(java.awt.event.ActionListener l) {
        runButton.addActionListener(l);
    }

    public void setAlgorithmOptions(java.util.Collection<String> names) {
        algoBox.removeAllItems();
        for (String n : names) algoBox.addItem(n);
    }

    public void bindArray(int[] data) {
        strip.setData(data);
    }

    public void highlightLine(int line) {
        pseudocode.highlight(line);
    }

    public void resetUI() {
        strip.repaint();
        steps = 0;
        costMeter.setMetric("Steps", steps);
        highlightLine(0);
    }

    // Event handling

    @Override
    public void onEvent(Event e) {
        switch (e.getType()) {
            case COMPARE, VISIT -> {
                int[] idx = e.getIndices();
                if (idx.length > 0) {
                    strip.flash(idx[0]);
                }
            }
            case SWAP -> {
                int[] idx = e.getIndices();
                if (idx.length >= 2) {
                    strip.swap(idx[0], idx[1]);
                }
            }
            case CUSTOM -> {
                if ("STEP".equals(e.getPayload())) {
                    steps++;
                    costMeter.setMetric("Steps", steps);
                }
            }
            default -> {
            }
        }
    }
} 