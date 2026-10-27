package edu.tue.dsvis.modules.heaps.view;

import edu.tue.dsvis.core.event.Event;
import edu.tue.dsvis.core.event.EventBus;
import edu.tue.dsvis.core.mvc.View;
import edu.tue.dsvis.gui.CostMeter;
import edu.tue.dsvis.gui.PseudocodePane;
import edu.tue.dsvis.widgets.HeapArrayStrip;
import edu.tue.dsvis.widgets.HeapTreePanel;
import edu.tue.dsvis.widgets.PriorityQueuePanel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class HeapsView implements View, EventBus.EventListener {

    private final JPanel root = new JPanel(new BorderLayout());

    // NORTH controls
    private final JTextField txtArrayInput = new JTextField(32);
    private final JButton btnRandom = new JButton("Random");
    private final JSpinner spnSize = new JSpinner(new SpinnerNumberModel(20, 1, 512, 1));
    private final JRadioButton radBottomUp = new JRadioButton("Bottom-up", true);
    private final JRadioButton radIncremental = new JRadioButton("Incremental");
    private final JButton btnBuild = new JButton("Build / Reset");
    private final JComboBox<String> cmbOperation = new JComboBox<>(new String[]{
            "Insert k", "Extract-Max", "Increase-Key(i,Δ)", "Decrease-Key(i,Δ)"});
    private final JButton btnRunOp = new JButton("Run op");
    private final JButton btnCompare = new JButton("Compare builds");
    private final JLabel statusLabel = new JLabel(" ");

    // CENTER visual components
    private final HeapArrayStrip arrayStrip = new HeapArrayStrip();
    private final HeapTreePanel treePanel = new HeapTreePanel(EventBus.getGlobal());
    private final PriorityQueuePanel pqPanel = new PriorityQueuePanel();
    private final PseudocodePane pseudocodePane = new PseudocodePane("/pseudocode/heap.txt");

    // SOUTH cost
    private final CostMeter costMeter = new CostMeter();
    private long steps = 0;

    public HeapsView() {
        buildUI();
        registerToBus();
    }

    private void buildUI() {
        // control bar: building on the first row, operations on the second
        JPanel buildRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buildRow.add(new JLabel("Initial array:"));
        buildRow.add(txtArrayInput);
        buildRow.add(btnRandom);
        buildRow.add(spnSize);
        buildRow.add(new JLabel("Build mode:"));
        buildRow.add(radBottomUp);
        buildRow.add(radIncremental);
        ButtonGroup grp = new ButtonGroup();
        grp.add(radBottomUp); grp.add(radIncremental);
        buildRow.add(btnBuild);

        JPanel operationRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        operationRow.add(new JLabel("Operation:"));
        operationRow.add(cmbOperation);
        operationRow.add(btnRunOp);
        operationRow.add(btnCompare);
        operationRow.add(statusLabel);

        JPanel bar = new JPanel(new GridLayout(2, 1));
        bar.add(buildRow);
        bar.add(operationRow);
        root.add(bar, BorderLayout.NORTH);

        // bind twin
        arrayStrip.bindTwin(treePanel);

        // splits
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(arrayStrip), new JScrollPane(treePanel));
        leftSplit.setResizeWeight(0.55);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                pqPanel, pseudocodePane);
        rightSplit.setResizeWeight(0.75);

        JSplitPane vizSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftSplit, rightSplit);
        vizSplit.setResizeWeight(0.65);
        root.add(vizSplit, BorderLayout.CENTER);

        root.add(costMeter, BorderLayout.SOUTH);

        // random button action
        btnRandom.addActionListener(e -> fillRandom());
    }

    private void registerToBus() {
        EventBus bus = EventBus.getGlobal();
        bus.register(this);
        bus.register(treePanel);
    }

    /** Stops listening to the event bus. */
    public void detach() { EventBus.getGlobal().unregister(this); EventBus.getGlobal().unregister(treePanel); }

    private void fillRandom() {
        int n = (Integer) spnSize.getValue();
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(',');
            sb.append(rnd.nextInt(1099) - 99); // -99..999
        }
        txtArrayInput.setText(sb.toString());
    }

    // ------------------- Public helpers for controller -------------------

    public void addBuildListener(java.awt.event.ActionListener l) { btnBuild.addActionListener(l); }
    public void addRunOpListener(java.awt.event.ActionListener l) { btnRunOp.addActionListener(l); }
    public void addCompareListener(java.awt.event.ActionListener l) { btnCompare.addActionListener(l); }

    public JPanel getRoot() { return root; }

    public int[] getInitialArray() {
        String text = txtArrayInput.getText().trim();
        if (text.isEmpty()) {
            fillRandom();
            text = txtArrayInput.getText();
        }
        try {
            String[] parts = text.split("[,\\s]+");
            int[] arr = new int[parts.length];
            for (int i = 0; i < parts.length; i++) arr[i] = Integer.parseInt(parts[i]);
            return arr;
        } catch (NumberFormatException ex) {
            txtArrayInput.setBackground(Color.PINK);
            javax.swing.Timer t = new javax.swing.Timer(600, e -> txtArrayInput.setBackground(Color.WHITE));
            t.setRepeats(false); t.start();
            return new int[0];
        }
    }

    public boolean isBottomUpSelected() { return radBottomUp.isSelected(); }

    public String getSelectedOperation() { return (String) cmbOperation.getSelectedItem(); }

    public void refreshHeap(int[] snap) {
        arrayStrip.setData(snap);
        treePanel.setHeap(snap);
        pqPanel.setData(snap);
    }

    public void resetView() {
        arrayStrip.clearHighlights();
        arrayStrip.clearSearchWindow();
        treePanel.onReset();
        steps = 0;
        costMeter.setMetric("Steps", steps);
        statusLabel.setText(" ");
        pseudocodePane.clearHighlight();
    }

    // ---------------- Event handling -----------------
    @Override
    public void onEvent(Event e) {
        switch (e.getType()) {
            case HIGHLIGHT_RANGE -> {
                int[] idx = e.getIndices();
                arrayStrip.flashRange(idx[0], idx[1]);
            }
            case LINE -> pseudocodePane.highlight(e.getIndices()[0]);
            case HEAP_STATE -> refreshHeap((int[]) e.getPayload());
            case COMPARE -> arrayStrip.flash(e.getIndices()[0]);
            case SWAP -> {
                int[] idx = e.getIndices();
                if (idx.length >= 2) {
                    arrayStrip.flash(idx[0]);
                    arrayStrip.flash(idx[1]);
                }
            }
            case RESULT -> statusLabel.setText(String.valueOf(e.getPayload()));
            case CUSTOM -> {
                if ("STEP".equals(e.getPayload())) {
                    steps++;
                    costMeter.setMetric("Steps", steps);
                }
            }
            default -> {}
        }
    }

    @Override public void onReset() { resetView(); }
}
