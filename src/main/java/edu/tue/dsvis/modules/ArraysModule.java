package main.java.edu.tue.dsvis.modules;

import main.java.edu.tue.dsvis.Main;
import main.java.edu.tue.dsvis.core.animation.Timeline;
import main.java.edu.tue.dsvis.core.event.EventBus;
import main.java.edu.tue.dsvis.widgets.ArrayStrip;

import javax.swing.*;
import java.awt.*;

/**
 * Demo module for array visualisation. Currently a stub with minimal UI.
 */
public class ArraysModule implements Main.MainModule {

    private final String name = "Arrays";
    private final JPanel root;
    private final Timeline timeline;

    public ArraysModule(EventBus bus, Timeline timeline) {
        this.timeline = timeline;
        this.root = buildUI(bus);
    }

    private JPanel buildUI(EventBus bus) {
        JPanel panel = new JPanel(new BorderLayout());
        ArrayStrip strip = new ArrayStrip();
        panel.add(strip, BorderLayout.CENTER);
        // For now feed sample data
        strip.setData(new int[]{3, 1, 4, 1, 5, 9, 2, 6});
        // TODO: Subscribe strip to EventBus for animations.
        return panel;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public JPanel root() {
        return root;
    }

    @Override
    public Timeline timeline() {
        return timeline;
    }
} 