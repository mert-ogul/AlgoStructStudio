package main.java.edu.tue.dsvis.gui.dialogs;

import javax.swing.*;
import java.awt.*;

/**
 * Utility dialog helpers for heap key operations.
 */
public final class KeyInputDialog {

    private KeyInputDialog() {}

    public static Integer showInsertDialog(Component owner) {
        String s = JOptionPane.showInputDialog(owner, "Enter key (int):", "Insert", JOptionPane.PLAIN_MESSAGE);
        try {
            return (s == null) ? null : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(owner, "Not an int", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static int[] showIncreaseDialog(Component owner) {
        JPanel panel = new JPanel(new java.awt.GridLayout(2, 2));
        JTextField idxField = new JTextField();
        JTextField deltaField = new JTextField();
        panel.add(new JLabel("Index:")); panel.add(idxField);
        panel.add(new JLabel("Δ (positive int):")); panel.add(deltaField);
        int res = JOptionPane.showConfirmDialog(owner, panel, "Increase-Key", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return null;
        try {
            int idx = Integer.parseInt(idxField.getText().trim());
            int delta = Integer.parseInt(deltaField.getText().trim());
            return new int[]{idx, delta};
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(owner, "Invalid", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public static int[] showDecreaseDialog(Component owner) {
        JPanel panel = new JPanel(new java.awt.GridLayout(2, 2));
        JTextField idxField = new JTextField();
        JTextField deltaField = new JTextField();
        panel.add(new JLabel("Index:")); panel.add(idxField);
        panel.add(new JLabel("Δ (positive int):")); panel.add(deltaField);
        int res = JOptionPane.showConfirmDialog(owner, panel, "Decrease-Key", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return null;
        try {
            int idx = Integer.parseInt(idxField.getText().trim());
            int delta = Integer.parseInt(deltaField.getText().trim());
            return new int[]{idx, delta};
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(owner, "Invalid", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
} 