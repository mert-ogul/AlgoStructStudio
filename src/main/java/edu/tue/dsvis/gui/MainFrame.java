package main.java.edu.tue.dsvis.gui;

import main.java.edu.tue.dsvis.modules.ModuleDescriptor;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application window hosting a module’s main panel and playback bar.
 */
public class MainFrame extends JFrame {

    /**
     * Constructs a main frame for the given module.
     *
     * @param module an instance of {@link main.java.edu.tue.dsvis.Main.MainModule}
     */
    public MainFrame(ModuleDescriptor module) {
        super("DS-Vis – " + module.name());
        setLayout(new BorderLayout());

        add(module.getRootPanel(), BorderLayout.CENTER);
        add(new PlaybackBar(module.getTimeline()), BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
} 