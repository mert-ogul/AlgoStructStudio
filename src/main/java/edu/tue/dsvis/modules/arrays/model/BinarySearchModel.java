package edu.tue.dsvis.modules.arrays.model;

import edu.tue.dsvis.core.animation.Frame;
import edu.tue.dsvis.core.mvc.Model;
import edu.tue.dsvis.core.event.Event;

/**
 * Binary search algorithm model that emits comparison and visit events via the
 * Timeline. Assumes the input array is sorted ascending.
 */
public class BinarySearchModel extends Model {

    private final int[] array;
    private final int target;

    public BinarySearchModel(int[] array, int target, ModelContext ctx) {
        super(ctx);
        this.array = array;
        this.target = target;
    }

    @Override
    public void run() {
        int low = 0;
        int high = array.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            int m = mid; // effectively final for lambda

            // Post current search window first
            int lowSnapshot = low;
            int highSnapshot = high;
            timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], new int[]{lowSnapshot, highSnapshot})), 0));

            // Highlight mid computation line (4)
            timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], 4)), 0));

            // Compare frame
            timeline.addFrame(Frame.blink(() -> {
                bus.post(Event.compare(m));
                bus.post(Event.step());
            }));

            if (array[mid] == target) {
                // Highlight found line (5)
                timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], 5)), 0));

                timeline.addFrame(new Frame(() -> bus.post(Event.visit(m)), 300));
                // Clear window after found
                timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], new int[]{-1, -2})), 0));
                timeline.addFrame(new Frame(() -> bus.post(Event.result("Found " + target + " at index " + m)), 0));
                return;
            } else if (array[mid] < target) {
                // Highlight low update branch line (7-8)
                timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], 7)), 0));
                low = mid + 1;
            } else {
                // Highlight high update branch line (9-10)
                timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], 9)), 0));
                high = mid - 1;
            }
        }

        // Not found: clear window, highlight return line (11)
        timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], new int[]{-1, -2})), 0));
        timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], 11)), 0));
        timeline.addFrame(new Frame(() -> bus.post(Event.result(target + " not found")), 0));
    }
}
