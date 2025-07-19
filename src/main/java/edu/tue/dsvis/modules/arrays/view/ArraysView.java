package main.java.edu.tue.dsvis.modules.arrays.view;

import main.java.edu.tue.dsvis.core.event.Event;
import main.java.edu.tue.dsvis.core.event.EventBus;
import main.java.edu.tue.dsvis.core.mvc.View;
import main.java.edu.tue.dsvis.gui.CostMeter;
import main.java.edu.tue.dsvis.gui.PseudocodePane;
import main.java.edu.tue.dsvis.widgets.ArrayStrip;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
    private final JCheckBox indexModeCheck = new JCheckBox("Index mode");

    private long steps = 0;  // cost metric counter

    public ArraysView(EventBus bus) {
        this.bus = bus;
        buildUI();
        bus.register(this);

        // Live update visualization when user edits array text
        arrayField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateStripFromText(); }
            @Override public void removeUpdate(DocumentEvent e) { updateStripFromText(); }
            @Override public void changedUpdate(DocumentEvent e) { updateStripFromText(); }
        });
    }

    private void buildUI() {
        // NORTH input bar
        JPanel inputBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputBar.add(new JLabel("Array:"));
        inputBar.add(arrayField);
        JLabel targetLabel = new JLabel("Target:");
        inputBar.add(targetLabel);
        inputBar.add(targetField);
        inputBar.add(indexModeCheck);
        inputBar.add(algoBox);
        inputBar.add(runButton);
        root.add(inputBar, BorderLayout.NORTH);

        // Show/hide target controls based on algorithm choice
        algoBox.addActionListener(e -> {
            boolean needsTarget = getSelectedAlgorithm().contains("Search");
            targetLabel.setVisible(needsTarget);
            targetField.setVisible(needsTarget);
            indexModeCheck.setVisible(needsTarget);
        });

        // Initial visibility based on default selection
        targetLabel.setVisible(false);
        targetField.setVisible(false);
        indexModeCheck.setVisible(false);

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

    public boolean isIndexMode() { return indexModeCheck.isSelected(); }

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
        updateArrayFieldFromStrip();
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
                    updateArrayFieldFromStrip();
                }
            }
            case SET_VALUE -> {
                int[] idx = e.getIndices();
                if (idx.length >= 1 && e.getPayload() instanceof Integer val) {
                    strip.setValue(idx[0], val);
                    updateArrayFieldFromStrip();
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

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void updateStripFromText() {
        try {
            int[] arr = parseArray(arrayField.getText());
            strip.setData(arr);
        } catch (NumberFormatException ignored) {
            // Ignore invalid input while editing
        }
    }

    private void updateArrayFieldFromStrip() {
        int[] data = strip.getData();
        arrayField.setText(arrayToString(data));
    }

    private int[] parseArray(String text) throws NumberFormatException {
        String[] parts = text.trim().split("[,\\s]+");
        if(parts.length==1 && parts[0].isEmpty()) return new int[0];
        int[] arr = new int[parts.length];
        for (int i=0;i<parts.length;i++) arr[i]=Integer.parseInt(parts[i]);
        return arr;
    }

    private String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<arr.length;i++) {
            if (i>0) sb.append(',');
            sb.append(arr[i]);
        }
        return sb.toString();
    }
} 