package edu.tue.dsvis.core.animation;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A very small animation timeline that owns a queue of {@link Frame}s and
 * drives them using a {@link javax.swing.Timer}. The timer fires on the AWT
 * event-dispatch thread (EDT), ensuring that frame tasks which mutate Swing
 * UI components run on the correct thread.
 *
 * <p>Minimal responsibilities:
 * <ul>
 *   <li>Play / pause / reset / rewind / single-step control</li>
 *   <li>Speed scaling between 0.05× and 4× real-time</li>
 *   <li>Each frame stays on screen for its own delay (at least
 *       {@link #MIN_FRAME_MS} at 1×); zero-delay frames run together with the
 *       next visible frame</li>
 *   <li>Property change notifications for the current playback position so
 *       UI widgets such as a scrubber can react</li>
 * </ul></p>
 *
 * <p>Future improvements:
 * <ul>
 *   <li>Add seek and step-back on top of the stored frame history</li>
 *   <li>Relay {@link edu.tue.dsvis.core.event.EventBus} events so
 *       that views can redraw by event rather than immediately mutating
 *       Swing components</li>
 * </ul></p>
 */
public class Timeline {

    /** Shortest time a visible frame stays on screen at 1× speed. */
    static final int MIN_FRAME_MS = 250;

    private final ConcurrentLinkedQueue<Frame> queue = new ConcurrentLinkedQueue<>();
    /** Every frame added since the last reset, so a run can be replayed. */
    private final List<Frame> history = new ArrayList<>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private double speedFactor = 1.0;
    private final Timer timer;

    private int position = 0; // number of executed frames, used for UI scrubber
    private boolean running = false;

    // Construction

    /**
     * Creates a new {@code Timeline}.
     */
    public Timeline() {
        this.timer = new Timer(0, e -> tick());
        this.timer.setRepeats(false);
    }

    // Public control API

    /** Starts playback if not already running. */
    public void start() {
        if (!running) {
            boolean old = running;
            running = true;
            timer.setInitialDelay(0);
            timer.restart();
            pcs.firePropertyChange("running", old, running);
        }
    }

    /** Pauses playback, keeping the current queue intact. */
    public void pause() {
        if (running) {
            timer.stop();
            boolean old = running;
            running = false;
            pcs.firePropertyChange("running", old, running);
        }
    }

    /**
     * Clears all queued frames and resets playback position to zero.
     * Does not automatically start playback.
     */
    public void reset() {
        pause();
        queue.clear();
        history.clear();
        int oldPos = position;
        position = 0;
        pcs.firePropertyChange("position", oldPos, position);
    }

    /**
     * Pauses and queues every frame since the last reset again, so playing
     * replays the run from the beginning. Models make their first frame
     * restore the starting state.
     */
    public void rewind() {
        pause();
        queue.clear();
        queue.addAll(history);
        int oldPos = position;
        position = 0;
        pcs.firePropertyChange("position", oldPos, position);
    }

    /**
     * Executes the next visible frame together with any zero-delay frames
     * before it, regardless of running state.
     */
    public void stepForward() {
        if (timer.isRunning()) {
            // Avoid double-running frames; pause first.
            pause();
        }
        tick();
    }

    /**
     * Adjusts playback speed. Values are clamped to the range [0.05, 4.0].
     *
     * @param factor the desired speed multiplier
     */
    public void setSpeed(double factor) {
        if (Double.isNaN(factor) || factor <= 0) {
            throw new IllegalArgumentException("factor must be positive");
        }
        double clamped = Math.max(0.05, Math.min(4.0, factor));
        this.speedFactor = clamped;
    }

    /** @return {@code true} when the timeline is actively playing. */
    public boolean isRunning() {
        return running;
    }

    // Queue management helpers

    /**
     * Enqueues a frame at the end of the timeline.
     *
     * @param frame non-null frame to add
     */
    public void addFrame(Frame frame) {
        Frame f = Objects.requireNonNull(frame);
        queue.add(f);
        history.add(f);
    }

    /** @return number of frames remaining in the queue. */
    public int getRemaining() {
        return queue.size();
    }

    // PropertyChangeSupport plumbing

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    // Internal helpers

    private void tick() {
        Frame frame = null;
        while (true) {
            frame = queue.poll();
            if (frame == null) {
                // No more frames – pause and notify listeners.
                pause();
                return;
            }
            runFrame(frame);
            if (frame.getDelayMs() > 0) {
                break;
            }
        }

        if (running) {
            timer.setInitialDelay((int) Math.round(Math.max(frame.getDelayMs(), MIN_FRAME_MS) / speedFactor));
            timer.restart();
        }
    }

    private void runFrame(Frame frame) {
        // Execute the frame's task. It should run on EDT; Timer already runs on EDT,
        // but guard against future call sites (e.g., stepForward from non-EDT).
        if (SwingUtilities.isEventDispatchThread()) {
            frame.getTask().run();
        } else {
            SwingUtilities.invokeLater(frame.getTask());
        }

        int oldPos = position;
        position++;
        pcs.firePropertyChange("position", oldPos, position);
    }
}
