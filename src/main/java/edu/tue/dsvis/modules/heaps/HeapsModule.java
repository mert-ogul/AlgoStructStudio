package edu.tue.dsvis.modules.heaps;

import edu.tue.dsvis.core.animation.Timeline;
import edu.tue.dsvis.core.event.EventBus;
import edu.tue.dsvis.modules.ModuleDescriptor;
import edu.tue.dsvis.modules.heaps.view.HeapsView;
import edu.tue.dsvis.modules.heaps.controller.HeapsController;

import javax.swing.*;

public class HeapsModule implements ModuleDescriptor {

    private final EventBus bus;
    private final Timeline timeline;
    private final HeapsView view;
    private final HeapsController ctrl;

    public HeapsModule(EventBus bus, Timeline timeline) {
        this.bus = bus;
        this.timeline = timeline;
        this.view = new HeapsView();
        this.ctrl = new HeapsController(bus, timeline, view);
        ctrl.initialise();
    }

    @Override public String name() { return "Heaps & Priority-Queues"; }

    @Override public JPanel getRootPanel() { return view.getRoot(); }

    @Override public Timeline getTimeline() { return timeline; }

    @Override public void reset() {
        timeline.reset();
        view.resetView();
    }

    @Override public void dispose() { timeline.reset(); view.detach(); }
} 