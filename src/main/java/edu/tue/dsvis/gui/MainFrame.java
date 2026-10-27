package edu.tue.dsvis.gui;

import edu.tue.dsvis.modules.ModuleDescriptor;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application window hosting a module’s main panel and playback bar.
 */
public class MainFrame extends JFrame {

    private ModuleDescriptor current;

    /**
     * Constructs a main frame for the given module.
     *
     * @param module the module shown when the window opens
     */
    public MainFrame(ModuleDescriptor module) {
        super("AlgoStruct Studio – " + module.name());
        this.current = module;
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
            edu.tue.dsvis.modules.arrays.ArraysModule am =
                    new edu.tue.dsvis.modules.arrays.ArraysModule(
                            edu.tue.dsvis.core.event.EventBus.getGlobal(), new edu.tue.dsvis.core.animation.Timeline());
            swapModule(content, am);
        });

        JMenuItem heapsItem = new JMenuItem("Heaps & Priority-Queues");
        heapsItem.addActionListener(e -> {
            edu.tue.dsvis.modules.heaps.HeapsModule hm =
                    new edu.tue.dsvis.modules.heaps.HeapsModule(
                            edu.tue.dsvis.core.event.EventBus.getGlobal(), new edu.tue.dsvis.core.animation.Timeline());
            swapModule(content, hm);
        });

        mModule.add(arraysItem);
        mModule.add(heapsItem);
        menuBar.add(mModule);
        setJMenuBar(menuBar);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void swapModule(JPanel content, edu.tue.dsvis.modules.ModuleDescriptor mod) {
        current.dispose();
        current = mod;
        content.removeAll();
        content.add(mod.getRootPanel(), BorderLayout.CENTER);
        getContentPane().remove(1); // remove old playback bar component (south)
        getContentPane().add(new edu.tue.dsvis.gui.PlaybackBar(mod.getTimeline()), BorderLayout.SOUTH);
        setTitle("AlgoStruct Studio – " + mod.name());
        validate();
        repaint();
    }
}
