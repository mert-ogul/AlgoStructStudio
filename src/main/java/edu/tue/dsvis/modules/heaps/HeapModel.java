package edu.tue.dsvis.modules.heaps;

import edu.tue.dsvis.core.animation.Frame;
import edu.tue.dsvis.core.animation.Timeline;
import edu.tue.dsvis.core.event.Event;
import edu.tue.dsvis.core.event.EventBus;
import edu.tue.dsvis.core.mvc.Model;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Array-based binary heap model with animation hooks and event emission so the
 * {@code HeapArrayStrip}, {@code HeapTreePanel}, and {@code PriorityQueuePanel}
 * stay in sync.
 *
 * <p>Operations update the heap immediately and queue frames that replay the
 * work. Every frame that changes the array carries a {@link Event#heapState}
 * snapshot, so views never draw a state the animation has not reached yet.</p>
 */
public class HeapModel extends Model {

    // ---------------------------------------------------------------------
    // Constants / tuning knobs
    // ---------------------------------------------------------------------
    public static final int MAX_HEAP_CAPACITY = 512;
    private static final int DEFAULT_ANIM_DELAY = 30; // ms

    // ---------------------------------------------------------------------
    // State
    // ---------------------------------------------------------------------
    private int[] heap = new int[MAX_HEAP_CAPACITY];
    private int heapSize = 0;

    private final boolean maxHeap = true; // future UI toggle

    private long compares = 0;
    private long swaps = 0;

    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------
    public HeapModel(ModelContext ctx) {
        super(ctx);
    }

    // package-private constructor for tests with custom capacity
    HeapModel(EventBus bus, Timeline timeline, int cap) {
        super(new ModelContext(bus, timeline));
        heap = new int[cap];
    }

    @Override public void run() { /* not used – controller drives specific ops */ }

    // ---------------------------------------------------------------------
    // Public API used by controller
    // ---------------------------------------------------------------------

    public void buildBottomUp(int[] src) {
        bus.post(Event.heapifyStart());

        heapSize = Math.min(src.length, MAX_HEAP_CAPACITY);
        System.arraycopy(src, 0, heap, 0, heapSize);
        emitState();

        for (int i = parent(heapSize - 1); i >= 0; i--) {
            siftDown(i, heapSize);
        }
        bus.post(Event.heapifyEnd());
    }

    public void buildIncremental(int[] src) {
        bus.post(Event.heapifyStart());
        heapSize = 0;
        emitState();
        for (int key : src) {
            insert(key);
        }
        bus.post(Event.heapifyEnd());
    }

    public void insert(int key) {
        if (heapSize >= MAX_HEAP_CAPACITY) {
            throw new IllegalStateException("Heap is full (" + MAX_HEAP_CAPACITY + " keys)");
        }
        heap[heapSize] = key;
        int idx = heapSize;
        heapSize++;
        emitLine(18);
        emitState(Event.insertKey(idx));
        siftUp(idx);
    }

    public int extractMax() {
        if (heapSize == 0) throw new NoSuchElementException("Heap is empty");
        int max = heap[0];
        emitLine(23);
        doSwapAnim(0, heapSize - 1);
        heapSize--;
        emitState();
        siftDown(0, heapSize);
        emitLine(26);
        timeline.addFrame(new Frame(() -> bus.post(Event.result("Extracted " + max)), 0));
        return max;
    }

    public void increaseKey(int idx, int delta) {
        checkKeyChange(idx, delta);
        heap[idx] += delta;
        emitLine(29);
        emitState(Event.keyUpdate(idx));
        siftUp(idx);
    }

    public void decreaseKey(int idx, int delta) {
        checkKeyChange(idx, delta);
        heap[idx] -= delta;
        emitLine(33);
        emitState(Event.keyUpdate(idx));
        siftDown(idx, heapSize);
    }

    public int[] getSnapshot() {
        return Arrays.copyOf(heap, heapSize);
    }

    public long getCompareCount() { return compares; }
    public long getSwapCount() { return swaps; }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    /**
     * Queues a zero-delay highlight of a line in heap.txt; it shows together
     * with the next visible frame.
     */
    private void emitLine(int line) {
        timeline.addFrame(new Frame(() -> bus.post(Event.line(line)), 0));
    }

    /** Queues a zero-delay frame that redraws the current heap. */
    private void emitState() {
        int[] snapshot = getSnapshot();
        timeline.addFrame(new Frame(() -> bus.post(Event.heapState(snapshot)), 0));
    }

    /** Queues a visible frame that redraws the current heap and highlights a node. */
    private void emitState(Event highlight) {
        int[] snapshot = getSnapshot();
        timeline.addFrame(new Frame(() -> {
            bus.post(Event.heapState(snapshot));
            bus.post(highlight);
        }, DEFAULT_ANIM_DELAY));
    }

    private void checkKeyChange(int idx, int delta) {
        if (idx < 0 || idx >= heapSize) throw new IllegalArgumentException("No key at index " + idx);
        if (delta <= 0) throw new IllegalArgumentException("Δ must be positive");
    }

    private void siftUp(int i) {
        while (i > 0) {
            int p = parent(i);
            compares++;
            emitLine(37);
            emitCompare(i, p);
            if (compare(heap[i], heap[p])) {
                emitLine(38);
                doSwapAnim(i, p);
                i = p;
            } else break;
        }
    }

    private void siftDown(int i, int size) {
        while (true) {
            int l = left(i);
            int r = right(i);
            int best = i;
            if (l < size) {
                compares++;
                emitLine(3);
                emitCompare(i, l);
                if (compare(heap[l], heap[best])) best = l;
            }
            if (r < size) {
                compares++;
                emitLine(5);
                emitCompare(i, r);
                if (compare(heap[r], heap[best])) best = r;
            }
            if (best != i) {
                emitLine(8);
                doSwapAnim(i, best);
                i = best;
            } else break;
        }
    }

    private void emitCompare(int i, int j) {
        timeline.addFrame(new Frame(() -> {
            bus.post(Event.compare(i));
            bus.post(Event.compare(j));
            bus.post(Event.step());
        }, DEFAULT_ANIM_DELAY));
    }

    private boolean compare(int child, int parentVal) {
        return maxHeap ? child > parentVal : child < parentVal;
    }

    private void doSwapAnim(int i, int j) {
        swaps++;
        swapInternal(i, j);
        int[] snapshot = getSnapshot();
        timeline.addFrame(new Frame(() -> {
            bus.post(Event.heapState(snapshot));
            bus.post(Event.swap(i, j));
            bus.post(Event.step());
        }, DEFAULT_ANIM_DELAY));
    }

    private void swapInternal(int i, int j) {
        int tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    private int parent(int i) { return (i - 1) / 2; }
    private int left(int i) { return 2 * i + 1; }
    private int right(int i) { return 2 * i + 2; }
}
