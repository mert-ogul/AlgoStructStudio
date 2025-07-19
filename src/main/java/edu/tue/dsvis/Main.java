package main.java.edu.tue.dsvis;

import main.java.edu.tue.dsvis.core.animation.Timeline;

import javax.swing.*;

/**
 * Application entry point placeholder. Provides {@code MainModule} record used
 * by {@link main.java.edu.tue.dsvis.gui.MainFrame}.
 */
public class Main {

    /** Convenience holder bundling together module metadata and root panel. */
    public interface MainModule {
        String name();
        JPanel root();
        Timeline timeline();
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        boolean headless = false;
        if (args != null) {
            for (String arg : args) {
                if ("--headless".equalsIgnoreCase(arg)) {
                    headless = true;
                    break;
                }
            }
        }

        if (headless) {
            runHeadlessDemo();
            return;
        }

        // Ensure system look and feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Bootstrap runtime objects
        main.java.edu.tue.dsvis.core.event.EventBus bus = main.java.edu.tue.dsvis.core.event.EventBus.getGlobal();
        Timeline timeline = new Timeline(60);

        // Instantiate the arrays module
        main.java.edu.tue.dsvis.modules.arrays.ArraysModule arrays = new main.java.edu.tue.dsvis.modules.arrays.ArraysModule(bus, timeline);

        javax.swing.SwingUtilities.invokeLater(() -> new main.java.edu.tue.dsvis.gui.MainFrame(arrays));
    }

    /**
     * Placeholder headless demo when running with --headless flag.
     */
    private static void runHeadlessDemo() {
        System.out.println("Running DS-Vis in headless demo mode. (No GUI)\nTODO: implement JUnit demo.");
    }
} 