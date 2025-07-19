package main.java.edu.tue.dsvis.modules.arrays.model;

import main.java.edu.tue.dsvis.core.animation.Frame;
import main.java.edu.tue.dsvis.core.event.Event;
import main.java.edu.tue.dsvis.core.mvc.Model;

/**
 * Insertion sort algorithm model that schedules visualisation frames on the
 * {@link main.java.edu.tue.dsvis.core.animation.Timeline}.
 */
public class InsertionSortModel extends Model {

    private final int[] a;

    public InsertionSortModel(int[] array, ModelContext ctx) {
        super(ctx);
        this.a = array.clone(); // work on a copy to preserve caller array
    }

    @Override
    public void run() {
        int n = a.length;
        for (int j = 1; j < n; j++) {
            // highlight outer loop line 1
            timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], 1)), 0));
            int key = a[j];
            int indexKey = j;

            // Highlight the key element
            timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], 2)), 0));
            timeline.addFrame(Frame.blink(() -> bus.post(Event.visit(indexKey))));
            // Custom cost metric increment – VISIT counts as a step
            timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], "STEP")), 0));

            int i = j - 1;
            while (i >= 0 && a[i] > key) {
                timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], 4)), 0));
                final int idxI = i;
                final int idxIp1 = i + 1;

                // Compare cost
                timeline.addFrame(Frame.blink(() -> bus.post(Event.compare(idxI))));
                timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], "STEP")), 0));

                // Swap elements in the internal array and emit SWAP event
                int temp = a[idxI];
                a[idxIp1] = temp;
                a[idxI] = key; // temporarily place key to idxI until loop ends

                timeline.addFrame(new Frame(() -> bus.post(Event.swap(idxI, idxIp1)), 16));
                timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], "STEP")), 0));

                i--;
            }

            int insertPos = i + 1;
            a[insertPos] = key;
            final int pos = insertPos;
            timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.SET_VALUE, new int[]{pos}, key)), 16));
            timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], 7)), 0));
            timeline.addFrame(new Frame(() -> bus.post(new Event(Event.EventType.CUSTOM, new int[0], "STEP")), 0));
        }
    }
} 