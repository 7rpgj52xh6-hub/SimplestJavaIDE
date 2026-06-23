package simplestJavaIDEpackage.Library.Debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import simplestJavaIDEpackage.Theme;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedProgram;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedProgram.MethodLocation;

/**
 * Right-hand live debugger panel: step the program forward one line at a time
 * (output and input happen as you step), scrub back through what already ran, or
 * run to the end. Shows the current line's variables. Deliberately minimal — the
 * step-and-inspect model behind real debuggers.
 *
 * @author Daniel Trageser
 */
public class DebugPanel extends JPanel {

  private final DefaultTableModel tableModel =
      new DefaultTableModel(new Object[] {"Variable", "Wert"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  private final JTable table = new JTable(tableModel);
  private final JSlider slider = new JSlider(0, 0, 0);
  private final JButton prevButton = new JButton("◀ Zurück");
  private final JButton nextButton = new JButton("Weiter ▶");
  private final JButton continueButton = new JButton("Bis Ende ▶▶");
  private final JButton closeButton = new JButton("Schließen");
  private final JLabel stepLabel = new JLabel(" ");
  private final JLabel statusLabel = new JLabel(" ");

  private final List<TraceStep> history = new ArrayList<>();
  private GeneratedProgram program;
  private DebugSession session;
  private Consumer<MethodLocation> onStep = location -> {};
  private Runnable onClose = () -> {};
  private int index = -1;
  private boolean finished;
  private boolean stepPending;
  private boolean adjusting;

  public DebugPanel() {
    super(new BorderLayout());
    setPreferredSize(new Dimension(310, 100));
    setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.PANEL_BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

    JLabel title = new JLabel("Debugger");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
    title.setAlignmentX(Component.LEFT_ALIGNMENT);
    stepLabel.setForeground(new Color(0x9AA0A6));
    statusLabel.setForeground(Theme.HINT_AMBER);
    JPanel header = new JPanel();
    header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
    title.setAlignmentX(Component.LEFT_ALIGNMENT);
    stepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    header.add(title);
    header.add(stepLabel);
    header.add(statusLabel);
    add(header, BorderLayout.NORTH);

    table.setFillsViewportHeight(true);
    table.setRowHeight(24);
    table.getColumnModel().getColumn(1).setCellRenderer(new MonospaceRenderer());
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    add(scrollPane, BorderLayout.CENTER);

    for (JButton button : new JButton[] {prevButton, nextButton, continueButton, closeButton}) {
      button.setFocusable(false);
    }
    prevButton.setToolTipText("Einen Schritt zurück ansehen (macht nichts rückgängig)");
    nextButton.setToolTipText("Führt die nächste Zeile aus");
    continueButton.setToolTipText("Lässt den Rest des Programms ohne Stepping durchlaufen");
    closeButton.setToolTipText("Beendet den Debugger");
    prevButton.addActionListener(e -> showStep(index - 1));
    nextButton.addActionListener(e -> onNext());
    continueButton.addActionListener(e -> onContinue());
    closeButton.addActionListener(e -> onClose.run());
    slider.addChangeListener(
        e -> {
          if (!adjusting) {
            showStep(slider.getValue());
          }
        });

    JPanel stepButtons = new JPanel(new GridLayout(1, 2, 6, 0));
    stepButtons.add(prevButton);
    stepButtons.add(nextButton);
    JPanel south = new JPanel();
    south.setLayout(new javax.swing.BoxLayout(south, javax.swing.BoxLayout.Y_AXIS));
    south.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
    slider.setAlignmentX(Component.LEFT_ALIGNMENT);
    stepButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
    continueButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    closeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    continueButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
    closeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
    stepButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    slider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
    south.add(slider);
    south.add(javax.swing.Box.createVerticalStrut(6));
    south.add(stepButtons);
    south.add(javax.swing.Box.createVerticalStrut(6));
    south.add(continueButton);
    south.add(javax.swing.Box.createVerticalStrut(6));
    south.add(closeButton);
    add(south, BorderLayout.SOUTH);
  }

  public void setOnClose(Runnable onClose) {
    this.onClose = onClose;
  }

  /** Begins a fresh debugging session (no steps recorded yet). */
  public void begin(
      GeneratedProgram program, DebugSession session, Consumer<MethodLocation> onStep) {
    this.program = program;
    this.session = session;
    this.onStep = onStep;
    history.clear();
    index = -1;
    finished = false;
    stepPending = false;
    tableModel.setRowCount(0);
    stepLabel.setText(" ");
    statusLabel.setText("Programm gestartet …");
    adjusting = true;
    slider.setMinimum(0);
    slider.setMaximum(0);
    slider.setValue(0);
    adjusting = false;
    updateButtons();
  }

  /** Called when the program pauses at a new line (on the EDT). */
  public void addStep(TraceStep step) {
    history.add(step);
    stepPending = false;
    statusLabel.setText(" ");
    index = history.size() - 1;
    adjusting = true;
    slider.setMaximum(Math.max(0, history.size() - 1));
    slider.setValue(index);
    adjusting = false;
    renderCurrent();
    updateButtons();
  }

  public void finished(boolean truncated) {
    finished = true;
    stepPending = false;
    statusLabel.setText(truncated ? "Lauf gekürzt (Limit erreicht)." : "Programm beendet.");
    updateButtons();
  }

  private void onNext() {
    if (index < history.size() - 1) {
      showStep(index + 1);
    } else if (!finished && !stepPending) {
      stepPending = true;
      statusLabel.setText("läuft …");
      updateButtons();
      session.step();
    }
  }

  private void onContinue() {
    if (!finished && !stepPending) {
      stepPending = true;
      statusLabel.setText("läuft bis zum Ende …");
      updateButtons();
      session.continueToEnd();
    }
  }

  private void showStep(int target) {
    if (history.isEmpty()) {
      return;
    }
    index = Math.max(0, Math.min(history.size() - 1, target));
    adjusting = true;
    slider.setValue(index);
    adjusting = false;
    renderCurrent();
    updateButtons();
  }

  private void renderCurrent() {
    if (index < 0 || index >= history.size()) {
      return;
    }
    TraceStep step = history.get(index);
    tableModel.setRowCount(0);
    for (Map.Entry<String, String> entry : step.variables().entrySet()) {
      tableModel.addRow(new Object[] {entry.getKey(), entry.getValue()});
    }
    MethodLocation location = program.locate(step.className(), step.generatedLine());
    String where;
    if (location != null && location.methodIndex() >= 0) {
      where =
          "Klasse '"
              + location.className()
              + "', Methode '"
              + location.methodName()
              + "', Zeile "
              + location.localLine();
    } else if (location != null) {
      where = "Klasse '" + location.className() + "', Zeile " + location.localLine();
    } else {
      where = "Zeile " + step.generatedLine();
    }
    stepLabel.setText("Schritt " + (index + 1) + " / " + history.size() + " · " + where);
    onStep.accept(location);
  }

  private void updateButtons() {
    boolean atFrontier = index >= history.size() - 1;
    prevButton.setEnabled(index > 0);
    nextButton.setEnabled(!stepPending && (!atFrontier || !finished));
    continueButton.setEnabled(!finished && !stepPending && atFrontier);
    slider.setEnabled(history.size() > 1);
  }

  private static class MonospaceRenderer extends javax.swing.table.DefaultTableCellRenderer {
    private static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      Component c =
          super.getTableCellRendererComponent(table, value, selected, focused, row, column);
      c.setFont(MONO);
      return c;
    }
  }
}
