package edu.tue.dsvis.modules.heaps.controller;

import edu.tue.dsvis.core.animation.Frame;
import edu.tue.dsvis.core.animation.Timeline;
import edu.tue.dsvis.core.event.EventBus;
import edu.tue.dsvis.core.mvc.Controller;
import edu.tue.dsvis.gui.dialogs.KeyInputDialog;
import edu.tue.dsvis.modules.heaps.HeapModel;
import edu.tue.dsvis.modules.heaps.view.HeapsView;

import javax.swing.JOptionPane;
import java.util.NoSuchElementException;

public class HeapsController implements Controller {

    private final EventBus bus;
    private final Timeline timeline;
    private final HeapsView view;
    private HeapModel model;

    public HeapsController(EventBus bus, Timeline timeline, HeapsView v) {
        this.bus = bus; this.timeline = timeline; this.view = v;
    }

    @Override
    public void initialise() {
        view.addBuildListener(e -> buildHeap());
        view.addRunOpListener(e -> runOperation());
        view.addCompareListener(e -> compareBuilds());
    }

    private void buildHeap() {
        timeline.pause();
        timeline.reset();
        view.resetView();

        int[] arr = view.getInitialArray();
        if (arr.length == 0) return;
        model = new HeapModel(new HeapModel.ModelContext(bus, timeline));
        // First frame clears the view, so a rewind replays the build from the beginning
        timeline.addFrame(new Frame(view::resetView, 0));
        if (view.isBottomUpSelected()) model.buildBottomUp(arr);
        else model.buildIncremental(arr);
        timeline.start();
    }

    private void runOperation() {
        if (model == null) {
            showWarning("Build a heap first.");
            return;
        }
        String op = view.getSelectedOperation();
        try {
            switch (op) {
                case "Insert k" -> {
                    Integer key = KeyInputDialog.showInsertDialog(view.getRoot());
                    if (key == null) return;
                    model.insert(key);
                }
                case "Extract-Max" -> model.extractMax();
                case "Increase-Key(i,Δ)" -> {
                    int[] vals = KeyInputDialog.showIncreaseDialog(view.getRoot());
                    if (vals == null) return;
                    model.increaseKey(vals[0], vals[1]);
                }
                case "Decrease-Key(i,Δ)" -> {
                    int[] vals = KeyInputDialog.showDecreaseDialog(view.getRoot());
                    if (vals == null) return;
                    model.decreaseKey(vals[0], vals[1]);
                }
            }
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException ex) {
            showWarning(ex.getMessage());
            return;
        }
        timeline.start();
    }

    /**
     * Builds the current input both ways on throwaway models (nothing is
     * animated) and shows how much work each strategy needed.
     */
    private void compareBuilds() {
        int[] arr = view.getInitialArray();
        if (arr.length == 0) return;

        HeapModel bottomUp = new HeapModel(new HeapModel.ModelContext(new EventBus(), new Timeline()));
        bottomUp.buildBottomUp(arr);
        HeapModel incremental = new HeapModel(new HeapModel.ModelContext(new EventBus(), new Timeline()));
        incremental.buildIncremental(arr);

        String msg = String.format("""
                %d keys

                Bottom-up: %d comparisons, %d swaps
                Incremental: %d comparisons, %d swaps

                Worst case: bottom-up O(n), incremental O(n log n).""",
                bottomUp.getSnapshot().length,
                bottomUp.getCompareCount(), bottomUp.getSwapCount(),
                incremental.getCompareCount(), incremental.getSwapCount());
        JOptionPane.showMessageDialog(view.getRoot(), msg, "Compare builds", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(view.getRoot(), msg, "Heap operation", JOptionPane.WARNING_MESSAGE);
    }
}
