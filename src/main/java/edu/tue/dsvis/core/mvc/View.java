package main.java.edu.tue.dsvis.core.mvc;

import main.java.edu.tue.dsvis.core.event.Event;

/**
 * Minimal contract for UI components interested in visualisation events.
 * <p>
 * Implementations are expected to be Swing components and therefore the
 * {@link #onEvent(Event)} callback will be invoked on the Swing EDT.
 * </p>
 */
public interface View {

    /**
     * Called when a new {@link Event} is published.
     *
     * @param e the event to handle (never {@code null})
     */
    void onEvent(Event e);

    /**
     * Notifies the view that the model has been reset. Default implementation
     * is a no-op; override if the view needs to clear state.
     */
    default void onReset() {
        // optional hook
    }

    // TODO: add void bindPlayback(Timeline t) when highlighting current frame
    //       position becomes necessary.
} 