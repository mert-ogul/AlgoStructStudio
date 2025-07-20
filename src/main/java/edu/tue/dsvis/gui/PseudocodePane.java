package main.java.edu.tue.dsvis.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Displays pseudocode loaded from a classpath resource and allows highlighting
 * of a single line number.
 *
 * <p>The component is read-only; callers should use {@link #highlight(int)} to
 * direct the user’s attention to the relevant line. All callbacks are expected
 * to be made on the Swing EDT.</p>
 */
public class PseudocodePane extends JScrollPane {

    private final JTextArea area;
    private final Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    private Object currentHighlight;
    private int currentLine = -1; // 1-based, -1 when none

    /**
     * Constructs a pane by loading the given resource from the classpath.
     *
     * @param resourcePath classpath path such as "/pseudocode/linear_search.txt"
     */
    public PseudocodePane(String resourcePath) {
        area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        setViewportView(area);

        loadResourceInternal(resourcePath);
    }

    // Public API

    /**
     * Highlights the specified 1-based line number, removing any previous
     * highlight. If the line is out of range, the call is ignored.
     */
    public void highlight(int line) {
        if (line == currentLine) return;
        Highlighter hl = area.getHighlighter();
        if (currentHighlight != null) {
            hl.removeHighlight(currentHighlight);
            currentHighlight = null;
        }
        currentLine = -1;

        if (line <= 0) return;
        Element root = area.getDocument().getDefaultRootElement();
        if (line > root.getElementCount()) return;

        Element lineElem = root.getElement(line - 1);
        int start = lineElem.getStartOffset();
        int end = lineElem.getEndOffset();
        try {
            currentHighlight = hl.addHighlight(start, end, painter);
            currentLine = line;
            // Ensure highlighted line is visible
            area.setCaretPosition(start);
        } catch (BadLocationException ignored) {
            // Should not happen as we validated indices.
        }
    }

    // Internal helpers

    private void loadResourceInternal(String path) {
        try (InputStream in = resolveStream(path)) {
            if (in == null) {
                area.setText("[pseudocode missing]");
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String content = br.lines().collect(Collectors.joining("\n"));
                    area.setText(content);
                    area.setCaretPosition(0);
                }
            }
        } catch (Exception e) {
            area.setText("[pseudocode missing]");
        }
    }

    /** Loads pseudocode from another classpath resource, clearing highlights. */
    public void loadResource(String resourcePath) {
        loadResourceInternal(resourcePath);
    }

    /** Backward-compat alias. */
    public void reload(String path) { loadResource(path); }

    /** Clears any existing line highlight. */
    public void clearHighlight() {
        if (currentHighlight != null) {
            area.getHighlighter().removeHighlight(currentHighlight);
            currentHighlight = null;
            currentLine = -1;
        }
    }

    private InputStream resolveStream(String path) {
        InputStream in = getClass().getResourceAsStream(path);
        if (in != null) return in;
        // Fallback: try filesystem relative to resources directory
        try {
            java.nio.file.Path p = java.nio.file.Paths.get("src/main/resources" + path);
            if (java.nio.file.Files.exists(p)) {
                return java.nio.file.Files.newInputStream(p);
            }
        } catch (Exception ignored) {}
        return null;
    }
} 