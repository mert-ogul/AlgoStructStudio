package main.java.edu.tue.dsvis.modules.arrays.controller;

import main.java.edu.tue.dsvis.core.animation.Timeline;
import main.java.edu.tue.dsvis.core.event.EventBus;
import main.java.edu.tue.dsvis.core.mvc.Model;
import main.java.edu.tue.dsvis.modules.arrays.view.ArraysView;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Controller that translates user input into algorithm model execution for the
 * Arrays module.
 */
public class ArraysController {

    private final EventBus bus;
    private final Timeline timeline;
    private final ArraysView view;

    private final Map<String, BiFunction<int[], Integer, Model>> modelFactories = new HashMap<>();

    public ArraysController(EventBus bus, Timeline timeline, ArraysView view) {
        this.bus = bus;
        this.timeline = timeline;
        this.view = view;
        registerModels();
        view.setAlgorithmOptions(modelFactories.keySet());
        view.addRunListener(e -> initialise());
        populateDefaults();
    }

    private void registerModels() {
        modelFactories.put("Linear Search", (arr, target) -> new main.java.edu.tue.dsvis.modules.arrays.model.LinearSearchModel(arr, target,
                new Model.ModelContext(bus, timeline)));
        modelFactories.put("Binary Search", (arr, target) -> new main.java.edu.tue.dsvis.modules.arrays.model.BinarySearchModel(arr, target, new Model.ModelContext(bus, timeline)));
        modelFactories.put("Insertion Sort", (arr, t) -> new main.java.edu.tue.dsvis.modules.arrays.model.InsertionSortModel(arr, new Model.ModelContext(bus, timeline)));
    }

    /** Parses the UI, builds the correct model and starts animation. */
    public void initialise() {
        int[] arr;
        int target;
        try {
            arr = parseArray(view.getArrayInput());
        } catch (NumberFormatException ex) {
            showError("Invalid array input. Use comma or space separated integers.");
            return;
        }

        target = 0;
        boolean needsTarget = view.getSelectedAlgorithm().contains("Search");
        if (needsTarget) {
            try {
                target = Integer.parseInt(view.getTargetInput().trim());
            } catch (NumberFormatException ex) {
                showError("Invalid target value/index.");
                return;
            }
        }

        String algo = view.getSelectedAlgorithm();
        BiFunction<int[], Integer, Model> factory = modelFactories.get(algo);
        if (factory == null) {
            showError("Algorithm not supported: " + algo);
            return;
        }

        view.bindArray(arr);
        timeline.reset();
        bus.post(new main.java.edu.tue.dsvis.core.event.Event(main.java.edu.tue.dsvis.core.event.Event.EventType.CUSTOM, new int[0], "start"));

        // If index mode selected, convert index to value
        if (needsTarget && view.isIndexMode()) {
            if (target < 0 || target >= arr.length) {
                showError("Index out of bounds.");
                return;
            }
            target = arr[target];
        }

        Model model = factory.apply(arr, target);
        model.run();
        timeline.start();
    }

    private int[] parseArray(String text) throws NumberFormatException {
        String[] parts = text.trim().split("[,\\s]+");
        int[] arr = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            arr[i] = Integer.parseInt(parts[i]);
        }
        return arr;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(view.getRoot(), msg, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Clears UI-specific state so that another run can start fresh. */
    public void resetUI() {
        timeline.reset();
        view.resetUI();
    }

    private void populateDefaults() {
        // Fill array field with some sample data and target
        int[] sample = new int[]{5,2,9,1,6};
        view.bindArray(sample);
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<sample.length;i++) { if(i>0) sb.append(","); sb.append(sample[i]); }
        view.setArrayInput(sb.toString());
        view.setTargetInput("9");
    }
} 