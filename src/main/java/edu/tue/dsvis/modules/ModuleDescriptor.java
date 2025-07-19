package main.java.edu.tue.dsvis.modules;

import main.java.edu.tue.dsvis.core.animation.Timeline;

import javax.swing.*;

/**
 * Mini service-provider interface for discoverable application modules.
 * Implementations should create and own their view hierarchy as well as
 * a dedicated {@link Timeline} instance.
 */
public interface ModuleDescriptor {

    /** @return the human-readable module name shown in the UI */
    String name();

    /**
     * Returns the root Swing component for this module. The panel must be fully
     * initialised and ready to be inserted into the main window.
     */
    JPanel getRootPanel();

    /** @return the {@link Timeline} controlling this module’s animation */
    Timeline getTimeline();

    /** Resets the module’s internal state; no-op for simple modules. */
    void reset();
} 