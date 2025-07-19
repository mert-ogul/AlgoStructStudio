package main.java.edu.tue.dsvis.core.animation;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
 *   <li>Play / pause / reset / single-step control</li>
 *   <li>Speed scaling between 0.25× and 4× real-time</li>
 *   <li>Property change notifications for the current playback position so
 *       UI widgets such as a scrubber can react</li>
 * </ul></p>
 *
 * <p>Future improvements:
 * <ul>
 *   <li>Add rewind / seek ability by storing executed frames</li>
 *   <li>Relay {@link main.java.edu.tue.dsvis.core.event.EventBus} events so
 *       that views can redraw by event rather than immediately mutating
 *       Swing components</li>
 * </ul></p>
 */
public class Timeline {

    private final ConcurrentLinkedQueue<Frame> queue = new ConcurrentLinkedQueue<>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final int basePeriodMs; // base period derived from fps
    private double speedFactor = 1.0;
    private final Timer timer;

    private int position = 0; // number of executed frames, used for UI scrubber

    // Construction

    /**
     * Creates a new {@code Timeline} with a desired frames-per-second rate.
     *
     * @param fps desired frames per second (>0)
     */
    public Timeline(int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("fps must be positive");
        }
        this.basePeriodMs = 1000 / fps;
        // The timer will be (re)configured when speed changes.
        this.timer = new Timer(basePeriodMs, e -> tick());
        this.timer.setRepeats(true);
    }

    // Public control API

    /** Starts playback if not already running. */
    public void start() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    /** Pauses playback, keeping the current queue intact. */
    public void pause() {
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    /**
     * Clears all queued frames and resets playback position to zero.
     * Does not automatically start playback.
     */
    public void reset() {
        pause();
        queue.clear();
        int oldPos = position;
        position = 0;
        pcs.firePropertyChange("position", oldPos, position);
    }

    /**
     * Executes exactly one frame if available, regardless of running state.
     */
    public void stepForward() {
        if (timer.isRunning()) {
            // Avoid double-running frames; pause first.
            pause();
        }
        tick();
    }

    /**
     * Adjusts playback speed. Values are clamped to the range [0.25, 4.0].
     *
     * @param factor the desired speed multiplier
     */
    public void setSpeed(double factor) {
        if (Double.isNaN(factor) || factor <= 0) {
            throw new IllegalArgumentException("factor must be positive");
        }
        double clamped = Math.max(0.25, Math.min(4.0, factor));
        if (this.speedFactor != clamped) {
            this.speedFactor = clamped;
            timer.setDelay((int) Math.round(basePeriodMs / speedFactor));
        }
    }

    /** @return {@code true} when the timeline is actively playing. */
    public boolean isRunning() {
        return timer.isRunning();
    }

    // Queue management helpers

    /**
     * Enqueues a frame at the end of the timeline.
     *
     * @param frame non-null frame to add
     */
    public void addFrame(Frame frame) {
        queue.add(Objects.requireNonNull(frame));
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
        Frame frame = queue.poll();
        if (frame == null) {
            // No more frames – pause and notify listeners.
            pause();
            return;
        }

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