package edu.tue.dsvis.modules.arrays;

import edu.tue.dsvis.core.animation.Timeline;
import edu.tue.dsvis.core.event.EventBus;
import edu.tue.dsvis.modules.ModuleDescriptor;
import edu.tue.dsvis.modules.arrays.view.ArraysView;
import edu.tue.dsvis.modules.arrays.controller.ArraysController;

import javax.swing.*;

/**
 * Arrays & Simple Sorts visualisation module.
 */
public class ArraysModule implements ModuleDescriptor {

    private final EventBus bus;
    private final Timeline timeline;
    private final ArraysView view;
    private final ArraysController ctrl;

    public ArraysModule(EventBus bus, Timeline timeline) {
        this.bus = bus;
        this.timeline = timeline;
        this.view = new ArraysView(bus);
        this.ctrl = new ArraysController(bus, timeline, view);
    }

    @Override
    public String name() {
        return "Arrays & Simple Sorts";
    }

    @Override
    public JPanel getRootPanel() {
        return view.getRoot();
    }

    @Override
    public Timeline getTimeline() {
        return timeline;
    }

    @Override
    public void reset() {
        timeline.reset();
        ctrl.resetUI();
    }

    @Override
    public void dispose() {
        timeline.reset();
        view.detach();
    }
} 