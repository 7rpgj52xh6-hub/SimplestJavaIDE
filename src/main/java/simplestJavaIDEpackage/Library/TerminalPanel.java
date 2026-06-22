package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedSource;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedSource.MethodLocation;
import simplestJavaIDEpackage.Library.Commands.CommandListener;
import simplestJavaIDEpackage.Library.Commands.JavaCompilerService;
import simplestJavaIDEpackage.Library.Commands.Runner;

/**
 * The bottom console: a toolbar (run / save / zoom / imports / help), a program
 * input line, and the output area. Drives compilation and running, and routes
 * compiler errors back to the matching method tab.
 *
 * @author Daniel Trageser
 */
public class TerminalPanel extends JPanel implements CommandListener {
  private static final long serialVersionUID = 4716862595957472820L;

  private final CodingFile codingFile;
  private JTextArea terminalOutput;
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
    userInputTextField.addActionListener(
        e -> {
          run(CommandType.INPUT);
          userInputTextField.setText(null);
        });
    JButton clearButton = new JButton("Clear");
    clearButton.setToolTipText("Clear the console");
    clearButton.addActionListener(e -> terminalOutput.setText(null));

    JPanel inputRow = new JPanel(new BorderLayout(6, 0));
    inputRow.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));
    inputRow.add(new JLabel("Input:"), BorderLayout.WEST);
    inputRow.add(userInputTextField, BorderLayout.CENTER);
    inputRow.add(clearButton, BorderLayout.EAST);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(toolBar, BorderLayout.NORTH);
    topPanel.add(inputRow, BorderLayout.SOUTH);

    // Output.
    terminalOutput = new JTextArea();
    terminalOutput.setEditable(false);
    terminalOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    DefaultCaret caret = (DefaultCaret) terminalOutput.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    JScrollPane outputScrollPane = new JScrollPane(terminalOutput);
    outputScrollPane.setFocusable(false);

    add(topPanel, BorderLayout.NORTH);
    add(outputScrollPane, BorderLayout.CENTER);
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

  public JTextArea getTextArea() {
    return terminalOutput;
  }

  public void setMethodTabsPanel(MethodTabsPanel methodTabsPanel) {
    this.methodTabsPanel = methodTabsPanel;
  }

  @Override
  public void commandOutput(String text) {
    // Called from the reader threads, so hand the UI update to the EDT.
    String annotated = annotateStackTrace(text);
    SwingUtilities.invokeLater(() -> terminalOutput.append(annotated));
  }

  @Override
  public void commandFailed(Exception exp) {
    SwingUtilities.invokeLater(
        () -> terminalOutput.append("Command failed - " + exp.getMessage() + "\n"));
  }

  @Override
  public void commandFinished() {
    SwingUtilities.invokeLater(() -> stopButton.setEnabled(false));
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
      terminalOutput.append(
          "No Java compiler found. Please run SimplestJavaIDE with a JDK (not just a JRE).\n");
      return;
    }
    if (result.success()) {
      run(CommandType.RUN);
      return;
    }
    reportDiagnostics(result, source);
  }

  /** Prints compiler messages, mapped back to the method tab and local line. */
  private void reportDiagnostics(JavaCompilerService.Result result, GeneratedSource source) {
    MethodLocation firstError = null;
    for (Diagnostic<? extends JavaFileObject> diagnostic : result.diagnostics()) {
      if (diagnostic.getKind() != Diagnostic.Kind.ERROR
          && diagnostic.getKind() != Diagnostic.Kind.WARNING) {
        continue;
      }
      int generatedLine = (int) diagnostic.getLineNumber();
      MethodLocation location = generatedLine > 0 ? source.locate(generatedLine) : null;
      String where;
      if (location != null) {
        where = "Method '" + location.methodName() + "', line " + location.localLine();
        if (firstError == null && diagnostic.getKind() == Diagnostic.Kind.ERROR) {
          firstError = location;
        }
      } else {
        where = "Line " + generatedLine;
      }
      terminalOutput.append(where + ": " + diagnostic.getMessage(null) + "\n");
    }
    if (firstError != null && methodTabsPanel != null) {
      methodTabsPanel.showError(firstError.methodIndex(), firstError.localLine());
    }
  }

  public void run(CommandType ct) {
    if (ct == CommandType.RUN) {
      // Stop a previous run before starting a new one (no piling-up processes).
      if (runner != null) {
        runner.kill();
      }
      String javaExecutable =
          System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
      List<String> commandValues =
          Arrays.asList(
              javaExecutable,
              "-cp",
              codingFile.generateClassPath(),
              codingFile.javaClass.className());
      runner = new Runner(this, commandValues);
      stopButton.setEnabled(true);
    } else if (ct == CommandType.INPUT) {
      if (runner == null) {
        return;
      }
      try {
        String command = this.userInputTextField.getText();
        runner.write(command + "\n");
      } catch (IOException ex) {
        ErrorPopupWindow.throwMessage("!! Failed to send command to process:" + ex.getMessage());
      }
    }
  }

  public enum CommandType {
    RUN,
    INPUT
  }
}
