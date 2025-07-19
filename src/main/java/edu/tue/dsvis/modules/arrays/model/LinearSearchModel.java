package main.java.edu.tue.dsvis.modules.arrays.model;

import main.java.edu.tue.dsvis.core.mvc.Model;
import main.java.edu.tue.dsvis.core.event.Event;

/**
 * Simple linear search algorithm model that emits compare events and stops
 * when the target value is found.
 */
public class LinearSearchModel extends Model {

    private final int[] array;
    private final int target;

    public LinearSearchModel(int[] array, int target, ModelContext ctx) {
        super(ctx);
        this.array = array;
        this.target = target;
    }

    @Override
    public void run() {
        for (int i = 0; i < array.length; i++) {
            int index = i;
            timeline.addFrame(new main.java.edu.tue.dsvis.core.animation.Frame(() ->
                    bus.post(main.java.edu.tue.dsvis.core.event.Event.compare(index)), 16));

            if (array[i] == target) {
                timeline.addFrame(new main.java.edu.tue.dsvis.core.animation.Frame(() ->
                        bus.post(main.java.edu.tue.dsvis.core.event.Event.visit(index)), 300));
                break;
            }
        }
    }
} 