package main.java.edu.tue.dsvis.modules.heaps;

import main.java.edu.tue.dsvis.core.mvc.Model;

import java.util.Arrays;

/**
 * Executes a bottom-up heap construction so that controllers can measure its
 * cost and compare with incremental building. It delegates the actual work to
 * an internal {@link HeapModel} instance that shares the same Timeline & EventBus.
 */
public final class BuildBottomUpModel extends Model {

    private final HeapModel heapModel;
    private final int[] input;

    public BuildBottomUpModel(ModelContext ctx, int[] src) {
        super(ctx);
        this.input = Arrays.copyOf(src, src.length);
        this.heapModel = new HeapModel(ctx);
    }

    @Override
    public void run() {
        heapModel.buildBottomUp(input);
        timeline.pause(); // stop playback so user can inspect result
    }

    public HeapModel getHeapModel() {
        return heapModel;
    }
} 