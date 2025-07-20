package main.java.edu.tue.dsvis.modules.heaps.controller;

import main.java.edu.tue.dsvis.core.animation.Timeline;
import main.java.edu.tue.dsvis.core.event.EventBus;
import main.java.edu.tue.dsvis.core.mvc.Controller;
import main.java.edu.tue.dsvis.modules.heaps.HeapModel;
import main.java.edu.tue.dsvis.modules.heaps.view.HeapsView;

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
    }

    private void buildHeap() {
        timeline.pause();
        timeline.reset();
        view.resetView();

        int[] arr = view.getInitialArray();
        if (arr.length == 0) return;
        model = new HeapModel(new HeapModel.ModelContext(bus, timeline));
        if (view.isBottomUpSelected()) model.buildBottomUp(arr);
        else model.buildIncremental(arr);
        view.bindPQModel(model);
        view.refreshHeap(model.getSnapshot());
        timeline.start();
    }

    private void runOperation() {
        if (model == null) return;
        String op = view.getSelectedOperation();
        switch (op) {
            case "Insert k" -> {
                int key = view.getOpKey(); if (key==Integer.MIN_VALUE) return; model.insertKey(key);
            }
            case "Extract-Max" -> model.extractMax();
            case "Increase-Key(i,Δ)" -> {
                int idx = view.getOpIndex(); int delta = view.getOpDelta(); if (idx<0||delta<=0) return; model.increaseKey(idx, delta);
            }
            case "Decrease-Key(i,Δ)" -> {
                int idx = view.getOpIndex(); int delta = view.getOpDelta(); if (idx<0||delta<=0) return; model.decreaseKey(idx, delta);
            }
        }
        view.refreshHeap(model.getSnapshot());
        timeline.start();
    }
} 