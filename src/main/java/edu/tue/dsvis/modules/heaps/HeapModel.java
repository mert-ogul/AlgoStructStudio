package main.java.edu.tue.dsvis.modules.heaps;

import main.java.edu.tue.dsvis.core.animation.Frame;
import main.java.edu.tue.dsvis.core.animation.Timeline;
import main.java.edu.tue.dsvis.core.event.Event;
import main.java.edu.tue.dsvis.core.event.EventBus;
import main.java.edu.tue.dsvis.core.mvc.Model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Array-based binary heap model with animation hooks and event emission so the
 * {@code HeapArrayStrip}, {@code HeapTreePanel}, and {@code PriorityQueuePanel}
 * stay in sync.
 */
public class HeapModel extends Model implements main.java.edu.tue.dsvis.widgets.PriorityQueuePanel.HeapModel {

    // ---------------------------------------------------------------------
    // Constants / tuning knobs
    // ---------------------------------------------------------------------
    public static final int MAX_HEAP_CAPACITY = 512;
    private static final int DEFAULT_ANIM_DELAY = 30; // ms
    private static final boolean FULL_ANIMATION = true;

    // ---------------------------------------------------------------------
    // State
    // ---------------------------------------------------------------------
    private int[] heap = new int[MAX_HEAP_CAPACITY];
    private int heapSize = 0;

    private final boolean maxHeap = true; // future UI toggle
    private boolean fibonacciVisualMode = false;

    private long compares = 0;
    private long swaps = 0;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

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
        timeline.reset();
        bus.post(Event.heapifyStart());

        heapSize = Math.min(src.length, MAX_HEAP_CAPACITY);
        System.arraycopy(src, 0, heap, 0, heapSize);

        for (int i = parent(heapSize - 1); i >= 0; i--) {
            siftDown(i, heapSize);
        }
        fireHeapChanged();
        bus.post(Event.heapifyEnd());
    }

    public void buildIncremental(int[] src) {
        timeline.reset();
        bus.post(Event.heapifyStart());
        heapSize = 0;
        for (int key : src) {
            insert(key);
        }
        bus.post(Event.heapifyEnd());
    }

    public void insert(int key) {
        if (heapSize >= MAX_HEAP_CAPACITY) {
            bus.post(new Event(Event.EventType.CUSTOM, null, "HEAP_FULL"));
            throw new IllegalStateException("Heap full");
        }
        heap[heapSize] = key;
        int idx = heapSize;
        heapSize++;
        siftUp(idx);
        fireHeapChanged();
    }

    // PriorityQueuePanel interface compliance
    @Override public int[] getArray() { return getSnapshot(); }
    @Override public void insertKey(int key) { insert(key); }

    public int extractMax() {
        if (heapSize == 0) throw new NoSuchElementException();
        int max = heap[0];
        swapInternal(0, heapSize - 1);
        heapSize--;
        siftDown(0, heapSize);
        fireHeapChanged();
        return max;
    }

    public void increaseKey(int idx, int delta) {
        if (delta <= 0) throw new IllegalArgumentException("delta must be positive");
        heap[idx] += delta;
        siftUp(idx);
        bus.post(Event.keyUpdate(idx));
        fireHeapChanged();
    }

    public void decreaseKey(int idx, int delta) {
        if (delta <= 0) throw new IllegalArgumentException("delta must be positive");
        heap[idx] -= delta;
        siftDown(idx, heapSize);
        bus.post(Event.keyUpdate(idx));
        fireHeapChanged();
    }

    public int[] getSnapshot() {
        return Arrays.copyOf(heap, heapSize);
    }

    public long getCompareCount() { return compares; }
    public long getSwapCount() { return swaps; }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private void siftUp(int i) {
        if (fibonacciVisualMode) return; // skip structural
        while (i > 0) {
            int p = parent(i);
            compares++;
            emitCompare(i, p);
            if (compare(heap[i], heap[p])) {
                doSwapAnim(i, p);
                i = p;
            } else break;
        }
    }

    private void siftDown(int i, int size) {
        if (fibonacciVisualMode) return;
        while (true) {
            int l = left(i);
            int r = right(i);
            int best = i;
            if (l < size) {
                compares++;
                emitCompare(i, l);
                if (compare(heap[l], heap[best])) best = l;
            }
            if (r < size) {
                compares++;
                emitCompare(i, r);
                if (compare(heap[r], heap[best])) best = r;
            }
            if (best != i) {
                doSwapAnim(i, best);
                i = best;
            } else break;
        }
    }

    private void emitCompare(int i, int j) {
        if (FULL_ANIMATION || heapSize <= 150) {
            timeline.addFrame(new Frame(() -> bus.post(Event.compare(i)), DEFAULT_ANIM_DELAY));
            timeline.addFrame(new Frame(() -> bus.post(Event.compare(j)), 0));
        }
    }

    private boolean compare(int child, int parentVal) {
        return maxHeap ? child > parentVal : child < parentVal;
    }

    private void doSwapAnim(int i, int j) {
        swaps++;
        if (FULL_ANIMATION || heapSize <= 150) {
            timeline.addFrame(new Frame(() -> bus.post(Event.swap(i, j)), DEFAULT_ANIM_DELAY));
        }
        swapInternal(i, j);
    }

    private void swapInternal(int i, int j) {
        int tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    private int parent(int i) { return (i - 1) / 2; }
    private int left(int i) { return 2 * i + 1; }
    private int right(int i) { return 2 * i + 2; }

    private void fireHeapChanged() {
        pcs.firePropertyChange("heap", null, getSnapshot());
    }

    // PropertyChangeSupport API
    public void addPropertyChangeListener(PropertyChangeListener l) { pcs.addPropertyChangeListener(l); }
    public void removePropertyChangeListener(PropertyChangeListener l) { pcs.removePropertyChangeListener(l); }
} 