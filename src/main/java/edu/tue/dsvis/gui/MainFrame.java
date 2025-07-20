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

        // Center area placeholder to allow swapping
        JPanel content = new JPanel(new BorderLayout());
        add(content, BorderLayout.CENTER);
        content.add(module.getRootPanel(), BorderLayout.CENTER);
        add(new PlaybackBar(module.getTimeline()), BorderLayout.SOUTH);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu mModule = new JMenu("Module");

        JMenuItem arraysItem = new JMenuItem("Arrays & Simple Sorts");
        arraysItem.addActionListener(e -> {
            main.java.edu.tue.dsvis.modules.arrays.ArraysModule am =
                    new main.java.edu.tue.dsvis.modules.arrays.ArraysModule(
                            main.java.edu.tue.dsvis.core.event.EventBus.getGlobal(), new main.java.edu.tue.dsvis.core.animation.Timeline(60));
            swapModule(content, am);
        });

        JMenuItem heapsItem = new JMenuItem("Heaps & Priority-Queues");
        heapsItem.addActionListener(e -> {
            main.java.edu.tue.dsvis.modules.heaps.HeapsModule hm =
                    new main.java.edu.tue.dsvis.modules.heaps.HeapsModule(
                            main.java.edu.tue.dsvis.core.event.EventBus.getGlobal(), new main.java.edu.tue.dsvis.core.animation.Timeline(60));
            swapModule(content, hm);
        });

        mModule.add(arraysItem);
        mModule.add(heapsItem);
        menuBar.add(mModule);
        setJMenuBar(menuBar);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void swapModule(JPanel content, main.java.edu.tue.dsvis.modules.ModuleDescriptor mod) {
        content.removeAll();
        content.add(mod.getRootPanel(), BorderLayout.CENTER);
        getContentPane().remove(1); // remove old playback bar component (south)
        getContentPane().add(new main.java.edu.tue.dsvis.gui.PlaybackBar(mod.getTimeline()), BorderLayout.SOUTH);
        setTitle("DS-Vis – " + mod.name());
        validate();
        repaint();
    }
} 