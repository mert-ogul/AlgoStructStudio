package main.java.edu.tue.dsvis.core.mvc;

import main.java.edu.tue.dsvis.core.animation.Frame;
import main.java.edu.tue.dsvis.core.animation.Timeline;
import main.java.edu.tue.dsvis.core.event.Event;
import main.java.edu.tue.dsvis.core.event.EventBus;

import java.util.Objects;

/**
 * Abstract base class for algorithm models. Provides convenience access to the
 * global {@link EventBus} and {@link Timeline}, plus small helper methods for
 * emitting common events while enqueuing animation {@link Frame}s.
 */
public abstract class Model {

    protected final EventBus bus;
    protected final Timeline timeline;

    /**
     * Constructs a model using the supplied execution context.
     */
    protected Model(ModelContext ctx) {
        Objects.requireNonNull(ctx, "ctx");
        this.bus = Objects.requireNonNull(ctx.bus(), "bus");
        this.timeline = Objects.requireNonNull(ctx.timeline(), "timeline");
    }

    /**
     * Implementations should enqueue ALL frames needed for the visualization
     * and return. The controller will subsequently invoke
     * {@link Timeline#start()} to play them back.
     */
    public abstract void run();

    // Convenience helpers for subclasses

    /**
     * Emits a COMPARE event for the given index and enqueues a tiny frame to
     * visually highlight it.
     */
    protected void flash(int index) {
        bus.post(Event.compare(index));
        timeline.addFrame(new Frame(() -> {
            /* no-op placeholder – real visualisation handled in view */
        }, 16)); // ~60 FPS
    }

    /**
     * Emits a SWAP event for the two indices and enqueues a frame placeholder.
     */
    protected void swap(int i, int j) {
        bus.post(Event.swap(i, j));
        timeline.addFrame(new Frame(() -> {
            /* no-op placeholder – real visualisation handled in view */
        }, 16));
    }

    // Optional extension points

    /**
     * Cancels a long-running computation. Default implementation is a no-op.
     */
    public void cancel() {
        // TODO subclasses may override when needed
    }

    /**
     * Resets the internal state of the model. Default implementation is a no-op.
     */
    public void reset() {
        // TODO subclasses may override when needed
    }

    // Context record

    /**
     * Aggregates external services provided by the application.
     */
    public record ModelContext(EventBus bus, Timeline timeline) {
        public ModelContext {
            Objects.requireNonNull(bus, "bus");
            Objects.requireNonNull(timeline, "timeline");
        }
    }
} 