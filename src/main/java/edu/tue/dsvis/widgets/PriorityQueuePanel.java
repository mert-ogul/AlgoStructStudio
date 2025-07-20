package main.java.edu.tue.dsvis.widgets;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Side widget that shows the heap contents in a table and offers basic
 * operations for interactive priority-queue demonstrations.
 */
public class PriorityQueuePanel extends JPanel implements PropertyChangeListener {

    /** Minimal interface needed from heap model. */
    public interface HeapModel {
        int[] getArray();
        void insertKey(int key);
        int extractMax();
        void increaseKey(int idx, int delta);
        void decreaseKey(int idx, int delta);
        void addPropertyChangeListener(PropertyChangeListener l);
    }

    private final JTable table;
    private final HeapTableModel tableModel;

    private HeapModel model;

    public PriorityQueuePanel() {
        super(new BorderLayout());
        tableModel = new HeapTableModel();
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setEnabled(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton insertBtn = new JButton("Insert");
        JButton extractBtn = new JButton("Extract-Max");
        JButton incBtn = new JButton("Increase-Key");
        JButton decBtn = new JButton("Decrease-Key");
        JButton batchBtn = new JButton("Batch…");

        buttons.add(insertBtn);
        buttons.add(extractBtn);
        buttons.add(incBtn);
        buttons.add(decBtn);
        buttons.add(batchBtn);
        add(buttons, BorderLayout.NORTH);

        // actions
        insertBtn.addActionListener(e -> doInsert());
        extractBtn.addActionListener(e -> doExtract());
        incBtn.addActionListener(e -> doAdjust(true));
        decBtn.addActionListener(e -> doAdjust(false));
        batchBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Batch not yet implemented"));
    }

    public void bindModel(HeapModel m) {
        if (this.model != null) this.model.addPropertyChangeListener(null);
        this.model = m;
        if (model != null) {
            model.addPropertyChangeListener(this);
            refresh();
        }
    }

    private void doInsert() {
        if (model == null) return;
        String input = JOptionPane.showInputDialog(this, "Key to insert:");
        if (input == null) return;
        try {
            int key = Integer.parseInt(input.trim());
            model.insertKey(key);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid integer.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExtract() {
        if (model == null) return;
        model.extractMax();
    }

    private void doAdjust(boolean increase) {
        if (model == null) return;
        String idxStr = JOptionPane.showInputDialog(this, "Index:");
        if (idxStr == null) return;
        String deltaStr = JOptionPane.showInputDialog(this, "Delta (positive integer):");
        if (deltaStr == null) return;
        try {
            int idx = Integer.parseInt(idxStr.trim());
            int delta = Integer.parseInt(deltaStr.trim());
            if (delta < 0) throw new NumberFormatException();
            if (increase) model.increaseKey(idx, delta); else model.decreaseKey(idx, delta);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refresh() {
        if (model == null) return;
        tableModel.setData(model.getArray());
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        if ("heap".equals(evt.getPropertyName())) refresh();
    }

    // --------------------------------------------------
    private static class HeapTableModel extends AbstractTableModel {
        private int[] data = new int[0];
        @Override public int getRowCount() { return data.length; }
        @Override public int getColumnCount() { return 2; }
        @Override public String getColumnName(int col) { return col==0?"Index":"Key"; }
        @Override public Object getValueAt(int row, int col) {
            return col==0?row:data[row];
        }
        public void setData(int[] d) { this.data = d; fireTableDataChanged(); }
        @Override public boolean isCellEditable(int r,int c){return false;}
    }
} 