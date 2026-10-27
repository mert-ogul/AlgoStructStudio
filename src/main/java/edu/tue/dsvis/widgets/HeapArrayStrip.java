package edu.tue.dsvis.widgets;

/**
 * Array visualisation specialised for heaps. It cooperates with
 * {@link HeapTreePanel} so that highlights stay in sync between strip and tree.
 */
public class HeapArrayStrip extends ArrayStrip {

    private HeapTreePanel twin;

    public void bindTwin(HeapTreePanel t) { this.twin = t; }

    @Override
    public void flash(int idx) {
        super.flash(idx);
        if (twin != null) twin.highlight(idx);
    }
}
