package main.java.edu.tue.dsvis.modules.arrays.view;

import main.java.edu.tue.dsvis.core.event.Event;
import main.java.edu.tue.dsvis.core.event.EventBus;
import main.java.edu.tue.dsvis.core.mvc.View;
import main.java.edu.tue.dsvis.gui.CostMeter;
import main.java.edu.tue.dsvis.gui.PseudocodePane;
import main.java.edu.tue.dsvis.widgets.ArrayStrip;
import main.java.edu.tue.dsvis.widgets.RecursionTreePanel;
import javax.swing.JSplitPane;
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
    private final RecursionTreePanel treePanel = new RecursionTreePanel();
    private final CostMeter costMeter = new CostMeter();

    private final java.util.Map<String,String> algoToCode = java.util.Map.of(
            "Linear Search", "/pseudocode/linear_search.txt",
            "Binary Search", "/pseudocode/binary_search.txt",
            "Insertion Sort", "/pseudocode/insertion_sort.txt");

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
        bus.register(treePanel);

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
            String sel = getSelectedAlgorithm();
            boolean needsTarget = getSelectedAlgorithm().contains("Search");
            targetLabel.setVisible(needsTarget);
            targetField.setVisible(needsTarget);
            indexModeCheck.setVisible(needsTarget);

            // reload pseudocode
            String res = algoToCode.getOrDefault(sel, "/pseudocode/linear_search.txt");
            pseudocode.reload(res);
        });

        // Initial visibility based on default selection
        targetLabel.setVisible(false);
        targetField.setVisible(false);
        indexModeCheck.setVisible(false);

        // CENTER visualisation split: array + recursion tree
        JSplitPane vizSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(strip),
                treePanel);
        vizSplit.setResizeWeight(0.8);
        root.add(vizSplit, BorderLayout.CENTER);

        // EAST split with pseudocode and cost meter
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(pseudocode),
                costMeter);
        rightSplit.setResizeWeight(0.7);
        root.add(rightSplit, BorderLayout.EAST);

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

    // ------------------------------------------------------------------
    // Recursion tree visibility
    // ------------------------------------------------------------------

    public void enableRecursionTree(boolean b) {
        treePanel.setVisible(b);
    }

    // ------------------------------------------------------------------
    // Reset view helpers
    // ------------------------------------------------------------------

    public void resetView() {
        strip.clearHighlights();
        strip.clearSearchWindow();
        treePanel.onReset();
        pseudocode.clearHighlight();
    }

    public JButton getRunButton() { return runButton; }

    public void addRunListener(java.awt.event.ActionListener l) {
        runButton.addActionListener(l);
    }

    public void setAlgorithmOptions(java.util.Collection<String> names) {
        algoBox.removeAllItems();
        for (String n : names) algoBox.addItem(n);
    }

    public void bindArray(int[] data) {
        strip.setData(data);
        treePanel.bindArray(data);
    }

    public void highlightLine(int line) {
        pseudocode.highlight(line);
    }

    public void resetUI() {
        strip.repaint();
        strip.clearSearchWindow();
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
                Object p = e.getPayload();
                if (p instanceof String str && "STEP".equals(str)) {
                    steps++;
                    costMeter.setMetric("Steps", steps);
                } else if (p instanceof int[] range && range.length==2) {
                    strip.setSearchWindow(range[0], range[1]);
                } else if (p instanceof Integer line) {
                    pseudocode.highlight(line);
                } else if (e.getType() == main.java.edu.tue.dsvis.core.event.Event.EventType.HIGHLIGHT_RANGE) {
                    int[] idx = e.getIndices();
                    if (idx.length==2) strip.flashRange(idx[0], idx[1]);
                } else if (e.getType() == main.java.edu.tue.dsvis.core.event.Event.EventType.LINE) {
                    int ln = e.getIndices()[0];
                    pseudocode.highlight(ln);
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

    // Allows controller to force reload when algorithm created programmatically
    public void reloadPseudocode(String resource) {
        pseudocode.reload(resource);
    }
} 