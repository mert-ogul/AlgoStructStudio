package main.java.edu.tue.dsvis.gui;

import main.java.edu.tue.dsvis.core.animation.Timeline;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Control strip providing play/pause, step, reset, and speed controls for a
 * {@link Timeline} instance.
 */
public class PlaybackBar extends JPanel {

    private final Timeline timeline;

    private final JButton playPauseBtn = new JButton("▶");
    private final JButton stepBtn = new JButton("▸▸");
    private final JButton resetBtn = new JButton("⟲");
    private final JSlider speedSlider = new JSlider(5, 400, 100); // 1x default (5==0.05x)

    public PlaybackBar(Timeline timeline) {
        this.timeline = timeline;
        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(playPauseBtn);
        add(stepBtn);
        add(resetBtn);
        add(new JLabel("Speed:"));
        add(speedSlider);

        // Button actions
        playPauseBtn.addActionListener(e -> togglePlayPause());
        stepBtn.addActionListener(e -> { timeline.stepForward(); updatePlayPauseIcon(); });
        resetBtn.addActionListener(e -> { timeline.reset(); updatePlayPauseIcon(); });
        speedSlider.addChangeListener(e -> {
            double factor = speedSlider.getValue() / 100.0;
            timeline.setSpeed(factor);
        });

        // Reflect external state changes (position property used as indicator)
        timeline.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                if ("running".equals(evt.getPropertyName())) {
                    updatePlayPauseIcon();
                }
            }
        });

        updatePlayPauseIcon();
    }

    private void togglePlayPause() {
        if (timeline.isRunning()) {
            timeline.pause();
        } else {
            timeline.start();
        }
        updatePlayPauseIcon();
    }

    private void updatePlayPauseIcon() {
        playPauseBtn.setText(timeline.isRunning() ? "❚❚" : "▶");
    }
} 