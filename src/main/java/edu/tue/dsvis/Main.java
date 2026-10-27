package edu.tue.dsvis;

import edu.tue.dsvis.core.animation.Timeline;
import edu.tue.dsvis.core.event.EventBus;
import edu.tue.dsvis.gui.MainFrame;
import edu.tue.dsvis.modules.arrays.ArraysModule;

import javax.swing.*;

/**
 * Application entry point.
 */
public class Main {

    /**
     * Application entry point.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        // Ensure system look and feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Swing components must be created on the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            EventBus bus = EventBus.getGlobal();
            Timeline timeline = new Timeline();
            new MainFrame(new ArraysModule(bus, timeline));
        });
    }
}
