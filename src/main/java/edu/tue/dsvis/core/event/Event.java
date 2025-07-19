package main.java.edu.tue.dsvis.core.event;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable value object representing a single animation-relevant occurrence.
 *
 * <p>The class is {@link Serializable} so that sequences of {@code Event}s can
 * later be streamed as JSON or through other serialization formats.</p>
 *
 * <p>TODO: add {@code timeStampNanos} for performance analysis.</p>
 */
public final class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Different kinds of events that can occur during a visualization.
     *
     * <p>Currently covers array-based algorithms; future modules can extend
     * this list with additional types such as {@code EDGE_RELAX},
     * {@code ROTATE}, or {@code UNION}.</p>
     */
    public enum EventType {
        COMPARE,
        SWAP,
        VISIT,
        SPLIT, // TODO: Later modules (range-tree etc.) may reuse SPLIT/MERGE for region splits.
        MERGE,
        LINE,               // 1-based pseudocode line highlight
        HIGHLIGHT_RANGE,    // visual highlight over array range
        HIGHLIGHT,
        SET_VALUE,
        CUSTOM
    }

    private final EventType type;
    private final int[] indices;   // affected array indices, may be empty
    private final Object payload;  // optional extra data (e.g. newKey for SET_VALUE)

    /**
     * Constructs a new {@code Event}.
     *
     * @param type    the kind of event
     * @param indices the indices affected by the event (may be {@code null} or empty)
     * @param payload optional extra data associated with the event (may be {@code null})
     */
    public Event(EventType type, int[] indices, Object payload) {
        this.type = Objects.requireNonNull(type, "type");
        // Defensive copy; treat null as an empty array for convenience
        this.indices = indices == null ? new int[0] : Arrays.copyOf(indices, indices.length);
        this.payload = payload; // may be null
    }

    // Getters

    public EventType getType() {
        return type;
    }

    /**
     * Returns a defensive copy of the indices array to preserve immutability.
     */
    public int[] getIndices() {
        return Arrays.copyOf(indices, indices.length);
    }

    public Object getPayload() {
        return payload;
    }

    // Static helper factories

    public static Event compare(int i) {
        return new Event(EventType.COMPARE, new int[]{i}, null);
    }

    public static Event swap(int i, int j) {
        return new Event(EventType.SWAP, new int[]{i, j}, null);
    }

    public static Event visit(int i) {
        return new Event(EventType.VISIT, new int[]{i}, null);
    }

    public static Event split(int left, int right) {
        return new Event(EventType.SPLIT, new int[]{left, right}, null);
    }

    public static Event merge(int left, int right) {
        return new Event(EventType.MERGE, new int[]{left, right}, null);
    }

    /**
     * Creates a LINE event used to highlight a 1-based pseudocode line number
     * in {@link main.java.edu.tue.dsvis.gui.PseudocodePane}.
     *
     * @param ln the 1-based line number to highlight
     */
    public static Event line(int ln) {
        return new Event(EventType.LINE, new int[]{ln}, null);
    }

    /**
     * Highlights a contiguous range [{@code left}, {@code right}] in the array
     * visualisation.
     */
    public static Event highlightRange(int left, int right) {
        return new Event(EventType.HIGHLIGHT_RANGE, new int[]{left, right}, null);
    }

    // TODO: when quiz functionality lands we can replay LINE events to
    //       PseudocodePane for step-by-step questions.

    // Object overrides

    @Override
    public String toString() {
        return "Event{" +
                "type=" + type +
                ", indices=" + Arrays.toString(indices) +
                ", payload=" + payload +
                '}';
    }
} 