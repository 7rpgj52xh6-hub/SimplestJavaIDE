package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Notifications;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedSource;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedSource.MethodLocation;
import simplestJavaIDEpackage.Library.Commands.CommandListener;
import simplestJavaIDEpackage.Library.Commands.CompilerHints;
import simplestJavaIDEpackage.Library.Commands.JavaCompilerService;
import simplestJavaIDEpackage.Library.Commands.Runner;

/**
 * The bottom console: a toolbar (run / stop / save / zoom / imports / help), a
 * program input line, and a colour-coded output area. Drives compilation and
 * running, shows the program lifecycle, and turns compiler errors into
 * beginner-friendly hints mapped back to the matching method tab.
 *
 * <p>Output is buffered and flushed on a timer so that a runaway loop (e.g.
 * {@code while(true) System.out.println(...)}) cannot flood the event thread and
 * make the UI — and the Stop button — unresponsive.
 *
 * @author Daniel Trageser
 */
public class TerminalPanel extends JPanel implements CommandListener {
  private static final long serialVersionUID = 4716862595957472820L;

  private static final Color OUTPUT_COLOR = new Color(225, 225, 225);
  private static final Color ERROR_COLOR = new Color(232, 104, 104);
  private static final Color SYSTEM_COLOR = new Color(120, 170, 120);
  private static final Color HINT_COLOR = new Color(225, 175, 90);
  private static final Color INPUT_COLOR = new Color(120, 150, 200);

  private static final int FLUSH_INTERVAL_MS = 50;
  private static final int MAX_PENDING_CHARS = 64_000;
  private static final int MAX_DOCUMENT_CHARS = 120_000;

  private final CodingFile codingFile;
  private final Deque<Segment> pending = new ArrayDeque<>();
  private int pendingChars;

  private JTextPane terminalOutput;
  private Style outputStyle;
  private Style errorStyle;
  private Style systemStyle;
  private Style hintStyle;
  private Style inputStyle;
  private Timer flushTimer;
  private Runner runner;
  private JTextField userInputTextField;
  private JButton saveButton;
  private JButton runButton;
  private JButton stopButton;
  private JButton helpButton;
  private JButton zoomInButton;
  private JButton zoomOutButton;
  private JButton btnAddImports;
  private MethodTabsPanel methodTabsPanel;
  private volatile GeneratedSource lastSource;

  private record Segment(String text, Style style) {}

  public TerminalPanel(CodingFile codingFile) {
    this.codingFile = codingFile;
    initializeUI();
  }

  private void initializeUI() {
    setLayout(new BorderLayout());

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

    runButton = toolButton("Run", KeyEvent.VK_R, "Compile and run (F5)");
    stopButton = toolButton("Stop", KeyEvent.VK_T, "Stop the running program");
    stopButton.setEnabled(false);
    stopButton.addActionListener(e -> stopRunningProgram());
    saveButton = toolButton("Save", KeyEvent.VK_S, "Save the project (Ctrl/Cmd+S)");
    zoomOutButton = toolButton("A-", KeyEvent.VK_MINUS, "Smaller font (Ctrl/Cmd+-)");
    zoomInButton = toolButton("A+", KeyEvent.VK_PLUS, "Larger font (Ctrl/Cmd++)");
    btnAddImports = toolButton("Add imports", KeyEvent.VK_I, "Manage imports");
    helpButton = toolButton("Help", KeyEvent.VK_H, "Help & about");

    toolBar.add(runButton);
    toolBar.add(stopButton);
    toolBar.add(saveButton);
    toolBar.addSeparator();
    toolBar.add(zoomOutButton);
    toolBar.add(zoomInButton);
    toolBar.addSeparator();
    toolBar.add(btnAddImports);
    toolBar.add(helpButton);

    // Program input line.
    userInputTextField = new JTextField();
    userInputTextField.setToolTipText("Type input for your running program and press Enter");
    userInputTextField.addActionListener(e -> sendInput());
    JButton clearButton = new JButton("Clear");
    clearButton.setToolTipText("Clear the console");
    clearButton.addActionListener(e -> clearConsole());

    JPanel inputRow = new JPanel(new BorderLayout(6, 0));
    inputRow.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));
    inputRow.add(new JLabel("Input:"), BorderLayout.WEST);
    inputRow.add(userInputTextField, BorderLayout.CENTER);
    inputRow.add(clearButton, BorderLayout.EAST);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(toolBar, BorderLayout.NORTH);
    topPanel.add(inputRow, BorderLayout.SOUTH);

    // Colour-coded output.
    terminalOutput = new JTextPane();
    terminalOutput.setEditable(false);
    terminalOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    terminalOutput.setBackground(new Color(30, 30, 30));
    outputStyle = makeStyle("output", OUTPUT_COLOR);
    errorStyle = makeStyle("error", ERROR_COLOR);
    systemStyle = makeStyle("system", SYSTEM_COLOR);
    hintStyle = makeStyle("hint", HINT_COLOR);
    inputStyle = makeStyle("input", INPUT_COLOR);
    DefaultCaret caret = (DefaultCaret) terminalOutput.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    JScrollPane outputScrollPane = new JScrollPane(terminalOutput);
    outputScrollPane.setFocusable(false);

    add(topPanel, BorderLayout.NORTH);
    add(outputScrollPane, BorderLayout.CENTER);

    flushTimer = new Timer(FLUSH_INTERVAL_MS, e -> flush());
    flushTimer.start();
  }

  private Style makeStyle(String name, Color color) {
    Style style = terminalOutput.addStyle(name, null);
    StyleConstants.setForeground(style, color);
    return style;
  }

  private JButton toolButton(String text, int mnemonic, String tooltip) {
    JButton button = new JButton(text);
    button.setMnemonic(mnemonic);
    button.setToolTipText(tooltip);
    button.setFocusable(false);
    return button;
  }

  public JButton getAddImportsButton() {
    return btnAddImports;
  }

  public JButton getSaveButton() {
    return saveButton;
  }

  public JButton getRunButton() {
    return runButton;
  }

  public JButton getHelpButton() {
    return helpButton;
  }

  public JButton getZoomInButton() {
    return zoomInButton;
  }

  public JButton getZoomOutButton() {
    return zoomOutButton;
  }

  public JTextPane getTextArea() {
    return terminalOutput;
  }

  public void setMethodTabsPanel(MethodTabsPanel methodTabsPanel) {
    this.methodTabsPanel = methodTabsPanel;
  }

  /**
   * Queues a styled chunk for output. Safe to call from any thread; the actual
   * document update happens on the EDT via the flush timer. The buffer is capped
   * so a runaway program drops the oldest output instead of overwhelming the UI.
   */
  private void enqueue(String text, Style style) {
    synchronized (pending) {
      pending.addLast(new Segment(text, style));
      pendingChars += text.length();
      while (pendingChars > MAX_PENDING_CHARS && !pending.isEmpty()) {
        pendingChars -= pending.pollFirst().text().length();
      }
    }
  }

  /** Drains the queued output into the document in batches (runs on the EDT). */
  private void flush() {
    List<Segment> batch;
    synchronized (pending) {
      if (pending.isEmpty()) {
        return;
      }
      batch = new ArrayList<>(pending);
      pending.clear();
      pendingChars = 0;
    }
    StyledDocument doc = terminalOutput.getStyledDocument();
    try {
      int i = 0;
      while (i < batch.size()) {
        Style style = batch.get(i).style();
        StringBuilder run = new StringBuilder();
        while (i < batch.size() && batch.get(i).style() == style) {
          run.append(batch.get(i).text());
          i++;
        }
        doc.insertString(doc.getLength(), run.toString(), style);
      }
      if (doc.getLength() > MAX_DOCUMENT_CHARS) {
        doc.remove(0, doc.getLength() - MAX_DOCUMENT_CHARS);
      }
      terminalOutput.setCaretPosition(doc.getLength());
    } catch (BadLocationException ignored) {
      // Positions are always valid here.
    }
  }

  private void clearConsole() {
    synchronized (pending) {
      pending.clear();
      pendingChars = 0;
    }
    terminalOutput.setText(null);
  }

  @Override
  public void commandOutput(String text, boolean error) {
    enqueue(annotateStackTrace(text), error ? errorStyle : outputStyle);
  }

  @Override
  public void commandFailed(Exception exp) {
    enqueue("Command failed - " + exp.getMessage() + "\n", errorStyle);
  }

  @Override
  public void commandFinished() {
    enqueue("✓ Programm beendet\n", systemStyle);
    SwingUtilities.invokeLater(() -> stopButton.setEnabled(false));
  }

  private void sendInput() {
    if (runner == null || !runner.isRunning()) {
      userInputTextField.setText(null);
      return;
    }
    String command = userInputTextField.getText();
    enqueue("> " + command + "\n", inputStyle); // echo so beginners see what they typed
    try {
      runner.write(command + "\n");
    } catch (IOException ex) {
      ErrorPopupWindow.throwMessage("Failed to send input to the program: " + ex.getMessage());
    }
    userInputTextField.setText(null);
  }

  /** Adds a [method:line] hint to stack-trace lines that point at generated code. */
  private String annotateStackTrace(String text) {
    GeneratedSource source = lastSource;
    if (source == null || !text.contains(".java:")) {
      return text;
    }
    Pattern pattern =
        Pattern.compile(Pattern.quote(codingFile.javaClass.className() + ".java") + ":(\\d+)");
    Matcher matcher = pattern.matcher(text);
    StringBuilder result = new StringBuilder();
    while (matcher.find()) {
      MethodLocation location = source.locate(Integer.parseInt(matcher.group(1)));
      String replacement =
          location == null
              ? matcher.group()
              : matcher.group() + " [" + location.methodName() + ":" + location.localLine() + "]";
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  public void stopRunningProgram() {
    if (runner != null) {
      runner.kill();
    }
  }

  /** Compiles the generated source in the background and reports the result. */
  public void compile() {
    if (methodTabsPanel != null) {
      methodTabsPanel.clearErrorHighlights();
    }
    clearConsole(); // fresh console for each run
    GeneratedSource source = codingFile.buildSource();
    lastSource = source; // used to annotate runtime stack traces
    codingFile.tmpSaveAndRunJavaCode();
    new Thread(
            () -> {
              JavaCompilerService.Result result =
                  JavaCompilerService.compile(
                      codingFile.getJavaTmpFilePath(), codingFile.generateClassPath());
              SwingUtilities.invokeLater(() -> onCompileFinished(result, source));
            })
        .start();
  }

  private void onCompileFinished(JavaCompilerService.Result result, GeneratedSource source) {
    if (!result.compilerAvailable()) {
      enqueue(
          "Kein Java-Compiler gefunden. Bitte SimplestJavaIDE mit einem JDK starten "
              + "(nicht nur einer JRE).\n",
          errorStyle);
      return;
    }
    if (result.success()) {
      run();
      return;
    }
    reportDiagnostics(result, source);
  }

  /** Prints compiler errors with a beginner hint, mapped back to the method tab. */
  private void reportDiagnostics(JavaCompilerService.Result result, GeneratedSource source) {
    MethodLocation firstError = null;
    int errorCount = 0;
    for (Diagnostic<? extends JavaFileObject> diagnostic : result.diagnostics()) {
      if (diagnostic.getKind() != Diagnostic.Kind.ERROR
          && diagnostic.getKind() != Diagnostic.Kind.WARNING) {
        continue;
      }
      if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
        errorCount++;
      }
      int generatedLine = (int) diagnostic.getLineNumber();
      MethodLocation location = generatedLine > 0 ? source.locate(generatedLine) : null;
      String where =
          location != null
              ? "Methode '" + location.methodName() + "', Zeile " + location.localLine()
              : "Zeile " + generatedLine;
      enqueue(where + ": " + diagnostic.getMessage(null) + "\n", errorStyle);
      String hint = CompilerHints.friendlyHint(diagnostic);
      if (hint != null) {
        enqueue("   💡 " + hint + "\n", hintStyle);
      }
      if (firstError == null && diagnostic.getKind() == Diagnostic.Kind.ERROR) {
        firstError = location;
      }
    }
    if (firstError != null && methodTabsPanel != null) {
      methodTabsPanel.showError(firstError.methodIndex(), firstError.localLine());
    }
    Notifications.error(errorCount + " Fehler — siehe Konsole");
  }

  private void run() {
    // Stop a previous run before starting a new one (no piling-up processes).
    if (runner != null) {
      runner.kill();
    }
    enqueue("▶ Programm gestartet\n", systemStyle);
    String javaExecutable =
        System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    List<String> commandValues =
        Arrays.asList(
            javaExecutable, "-cp", codingFile.generateClassPath(), codingFile.javaClass.className());
    runner = new Runner(this, commandValues);
    stopButton.setEnabled(true);
  }
}
