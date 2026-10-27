package edu.tue.dsvis.modules.arrays.model;

import edu.tue.dsvis.core.animation.Frame;
import edu.tue.dsvis.core.event.Event;
import edu.tue.dsvis.core.mvc.Model;

/**
 * Top-down recursive merge sort model. Emits SPLIT/MERGE events for the
 * recursion tree and SET_VALUE/COMPARE events for the array visualisation.
 */
public final class MergeSortModel extends Model {

    private final int[] a;
    private final int[] aux;

    public MergeSortModel(ModelContext ctx, int[] array) {
        super(ctx);
        this.a = array;
        this.aux = new int[array.length];
    }

    @Override
    public void run() {
        timeline.addFrame(new Frame(() -> bus.post(Event.line(1)), 0)); // header
        mergeSort(0, a.length - 1);
        // final highlight
        timeline.addFrame(new Frame(() -> bus.post(Event.highlightRange(0, a.length-1)), 100));
    }

    private void mergeSort(int l, int r) {
        if (l >= r) return;
        int mid = (l + r) / 2;

        timeline.addFrame(new Frame(() -> bus.post(Event.line(2)), 0)); // if check

        // SPLIT event for RecursionTreePanel
        timeline.addFrame(new Frame(() -> bus.post(Event.split(l, r)), 0));

        timeline.addFrame(new Frame(() -> bus.post(Event.line(3)), 30)); // compute mid

        timeline.addFrame(new Frame(() -> bus.post(Event.line(4)), 0));
        mergeSort(l, mid);
        timeline.addFrame(new Frame(() -> bus.post(Event.line(5)), 0));
        mergeSort(mid + 1, r);

        timeline.addFrame(new Frame(() -> bus.post(Event.line(6)), 0));

        merge(l, mid, r);

        // MERGE event
        timeline.addFrame(new Frame(() -> bus.post(Event.merge(l, r)), 0));
    }

    private void merge(int l, int m, int r) {
        // line 8
        timeline.addFrame(new Frame(() -> bus.post(Event.line(8)), 0));

        // copy to aux
        System.arraycopy(a, l, aux, l, r - l + 1);

        timeline.addFrame(new Frame(() -> bus.post(Event.line(10)), 0));

        int i = l, j = m + 1, k = l;
        timeline.addFrame(new Frame(() -> bus.post(Event.line(11)), 0));
        while (i <= m && j <= r) {
            int ii = i, jj = j, kk = k; // effectively final for lambdas
            timeline.addFrame(Frame.blink(() -> {
                bus.post(Event.line(16));
                bus.post(Event.compare(ii));
                bus.post(Event.compare(jj));
                bus.post(Event.step());
            }));

            if (aux[i] <= aux[j]) {
                int val = aux[i++];
                int dst = k;
                a[dst] = val; // mutate immediately
                timeline.addFrame(new Frame(() -> {
                    bus.post(Event.line(17));
                    bus.post(new Event(Event.EventType.SET_VALUE, new int[]{dst}, val));
                    bus.post(Event.step());
                }, (a.length>64)?0:30));
            } else {
                int val = aux[j++];
                int dst = k;
                a[dst] = val;
                timeline.addFrame(new Frame(() -> {
                    bus.post(Event.line(19));
                    bus.post(new Event(Event.EventType.SET_VALUE, new int[]{dst}, val));
                    bus.post(Event.step());
                }, (a.length>64)?0:30));
            }
            k++;
        }

        // copy remaining left side (lines 14-15)
        while (i <= m) {
            int val = aux[i++];
            int dst = k;
            a[dst] = val;
            timeline.addFrame(new Frame(() -> {
                bus.post(Event.line(15));
                bus.post(new Event(Event.EventType.SET_VALUE, new int[]{dst}, val));
                bus.post(Event.step());
            }, (a.length>64)?0:20));
            k++;
        }

        // copy remaining right side (lines 12-13 case)
        while (j <= r) {
            int val = aux[j++];
            int dst = k;
            a[dst] = val;
            timeline.addFrame(new Frame(() -> {
                bus.post(Event.line(13));
                bus.post(new Event(Event.EventType.SET_VALUE, new int[]{dst}, val));
                bus.post(Event.step());
            }, (a.length>64)?0:20));
            k++;
        }
    }
}
