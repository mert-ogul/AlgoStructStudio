package main.java.edu.tue.dsvis.core.event;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Very light pub-sub so that {@code Model}s emit events and any {@code View}
 * (or logger) can listen without a heavy framework dependency.
 *
 * <p>The bus is intentionally minimal: a thread-safe listener list and a
 * blocking {@link #post(Event)} method that delivers events to registered
 * listeners in registration order.</p>
 *
 * <p>Thread safety is provided via {@link CopyOnWriteArrayList}, allowing the
 * UI thread to subscribe/unsubscribe while the animation/model thread is
 * posting events.</p>
 *
 * <p>TODO: when Timeline rewind is implemented, add ability to “replay” events
 * to new listeners.</p>
 */
public class EventBus {

    /**
     * Functional listener interface – implementations can be given via
     * lambda/ method-reference.
     */
    @FunctionalInterface
    public interface EventListener {
        void onEvent(Event e);
    }

    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();

    private static final EventBus GLOBAL = new EventBus();

    /**
     * Convenience accessor for a global, process-wide bus instance.
     */
    public static EventBus getGlobal() {
        return GLOBAL;
    }

    // Registration API

    /**
     * Registers a listener if it is not already present.
     *
     * @param listener the listener to add – must not be {@code null}
     */
    public void register(EventListener listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.addIfAbsent(listener);
    }

    /**
     * Unregisters a previously registered listener.
     *
     * @param listener the listener to remove – must not be {@code null}
     */
    public void unregister(EventListener listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.remove(listener);
    }

    // Event dispatching

    /**
     * Posts an event to all registered listeners in their registration order.
     *
     * @param event the event to be delivered – must not be {@code null}
     */
    public void post(Event event) {
        Objects.requireNonNull(event, "event");
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
} 