package main.java.edu.tue.dsvis.modules.arrays.model;

import main.java.edu.tue.dsvis.core.animation.Frame;
import main.java.edu.tue.dsvis.core.mvc.Model;
import main.java.edu.tue.dsvis.core.event.Event;

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

            // Compare frame
            timeline.addFrame(Frame.blink(() -> bus.post(Event.compare(m))));

            if (array[mid] == target) {
                timeline.addFrame(new Frame(() -> bus.post(Event.visit(m)), 300));
                break;
            } else if (array[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
    }
} 