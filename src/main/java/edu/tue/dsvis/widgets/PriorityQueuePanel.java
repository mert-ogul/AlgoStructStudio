package edu.tue.dsvis.widgets;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

/**
 * Side widget that shows the heap contents as a read-only index/key table.
 * Operations are started from the heap module's control bar.
 */
public class PriorityQueuePanel extends JPanel {

    private final HeapTableModel tableModel;

    public PriorityQueuePanel() {
        super(new BorderLayout());
        tableModel = new HeapTableModel();
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setEnabled(false);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /** Shows the given heap array. */
    public void setData(int[] heap) { tableModel.setData(heap); }

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
