package main.java.edu.tue.dsvis.core.animation;

import java.util.Objects;

/**
 * Represents a single item in the animation timeline queue.
 *
 * <p>A {@code Frame} encapsulates a {@link Runnable} that mutates the UI and an
 * associated preferred display duration in milliseconds.</p>
 *
 * <p>Future work:
 * <ul>
 *   <li>Consider migrating to {@code record} (Java&nbsp;16+) for conciseness.</li>
 *   <li>Add {@code label} for debugging overlays (e.g. “Swap i&lt;-&gt;j”).</li>
 * </ul>
 * </p>
 */
public final class Frame {

    /** Runnable that applies UI changes for this frame. */
    private final Runnable task;

    /** Preferred display duration in milliseconds. */
    private final int delayMs;

    /** Default constructor. */
    public Frame(Runnable task, int delayMs) {
        this.task = Objects.requireNonNull(task, "task");
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs must be non-negative");
        }
        this.delayMs = delayMs;
    }

    // Accessors

    public Runnable getTask() {
        return task;
    }

    public int getDelayMs() {
        return delayMs;
    }

    // Static factory helpers

    /**
     * Convenience factory that returns a frame with a 200&nbsp;ms duration,
     * suitable for simple blink effects.
     */
    public static Frame blink(Runnable task) {
        return new Frame(task, 200);
    }

    // Object overrides

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Frame other)) return false;
        return delayMs == other.delayMs && task.equals(other.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, delayMs);
    }

    @Override
    public String toString() {
        return "Frame{" +
                "delayMs=" + delayMs +
                ", task=" + task +
                '}';
    }
} 