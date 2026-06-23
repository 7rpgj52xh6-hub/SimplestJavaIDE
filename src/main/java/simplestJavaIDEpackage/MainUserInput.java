package simplestJavaIDEpackage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import simplestJavaIDEpackage.Library.AddImportsWindow;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.FileManager;
import simplestJavaIDEpackage.Library.ClassTabsPanel;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedProgram;
import simplestJavaIDEpackage.Library.CodingArea;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedProgram.MethodLocation;
import simplestJavaIDEpackage.Library.Commands.JavaCompilerService;
import simplestJavaIDEpackage.Library.Debug.DebugPanel;
import simplestJavaIDEpackage.Library.Debug.DebugSession;
import simplestJavaIDEpackage.Library.Debug.TraceStep;
import simplestJavaIDEpackage.Library.TerminalPanel;

/**
 * @author Daniel Trageser This class implements the main user interface and its functions
 */
public class MainUserInput {

  private static final int MIN_FONT_SIZE = 10;
  private static final int MAX_FONT_SIZE = 60;

  private final CodingFile codingFile;
  private JFrame frmSimplestJavaIDE;
  private TerminalPanel terminal;
  private ClassTabsPanel codeTabs;
  private DebugPanel debugPanel;
  private ErrorListPanel errorListPanel;
  private StatusBar statusBar;
  private Timer errorCheckTimer;
  private Timer autosaveTimer;

  /** Create the application. */
  public MainUserInput(CodingFile savefile) {
    this.codingFile = savefile;
    initialize();
  }

  /** Launch the application. */
  public static void launch(CodingFile savefile) {
    EventQueue.invokeLater(
        () -> {
          try {
            MainUserInput window = new MainUserInput(savefile);
            window.frmSimplestJavaIDE.setVisible(true);
          } catch (Exception e) {
            ErrorPopupWindow.throwMessage(e.getMessage());
          }
        });
  }

  /** Initialize the contents of the frame. */
  private void initialize() {
    frmSimplestJavaIDE = new JFrame();
    frmSimplestJavaIDE.setMinimumSize(new Dimension(900, 600));
    Rectangle savedBounds = AppPreferences.getWindowBounds();
    if (savedBounds != null) {
      frmSimplestJavaIDE.setBounds(savedBounds);
    } else {
      frmSimplestJavaIDE.setSize(1080, 720);
      frmSimplestJavaIDE.setLocationRelativeTo(null);
    }
    frmSimplestJavaIDE.getContentPane().setLayout(new BorderLayout(0, 0));
    frmSimplestJavaIDE.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    frmSimplestJavaIDE.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            handleClose();
          }
        });

    setWindowIcon();

    terminal = new TerminalPanel(codingFile);
    codeTabs = new ClassTabsPanel(codingFile, terminal);

    // Toolbar at the top of the window, methods and console split below.
    frmSimplestJavaIDE.getContentPane().add(terminal.getToolBar(), BorderLayout.NORTH);

    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    mainSplitPane.setBorder(null);
    mainSplitPane.setResizeWeight(0.8);
    mainSplitPane.setTopComponent(codeTabs);
    mainSplitPane.setBottomComponent(terminal);
    frmSimplestJavaIDE.getContentPane().add(mainSplitPane, BorderLayout.CENTER);
    mainSplitPane.setDividerLocation(frmSimplestJavaIDE.getHeight() - 300);

    statusBar = new StatusBar();
    Notifications.setStatusBar(statusBar);
    errorListPanel = new ErrorListPanel();
    errorListPanel.setOnSelect(this::jumpToError);
    JPanel southPanel = new JPanel(new BorderLayout());
    southPanel.add(errorListPanel, BorderLayout.NORTH);
    southPanel.add(statusBar, BorderLayout.SOUTH);
    frmSimplestJavaIDE.getContentPane().add(southPanel, BorderLayout.SOUTH);

    errorCheckTimer = new Timer(600, e -> checkErrors());
    errorCheckTimer.setRepeats(false);
    autosaveTimer = new Timer(20_000, e -> autosave());
    autosaveTimer.start();

    debugPanel = new DebugPanel();
    debugPanel.setOnClose(this::closeDebug);
    terminal.setOnDebugStarted(this::openDebugPanel);
    terminal.setDebugUiListener(
        new DebugSession.Listener() {
          @Override
          public void onPaused(TraceStep step) {
            debugPanel.addStep(step);
          }

          @Override
          public void onFinished(boolean truncated) {
            debugPanel.finished(truncated);
          }

          @Override
          public void onError(String message) {
            // The message is already shown in the console.
          }
        });

    wireActions();
    installShortcuts();

    // A freshly loaded file is not "unsaved" even though setting the editor text
    // flips the save button on during construction.
    terminal.getSaveButton().setEnabled(false);
    terminal.getSaveButton().addPropertyChangeListener("enabled", e -> updateTitle());
    updateTitle();

    applyFontSize(AppPreferences.getFontSize(codeTabs.getReferenceFont().getSize()));
  }

  private void wireActions() {
    terminal.getHelpButton().addActionListener(e -> ImprintWindow.main(null));
    terminal.getSaveButton().addActionListener(e -> saveProject());
    terminal.getRunButton().addActionListener(e -> runProject());
    terminal.getZoomInButton().addActionListener(e -> applyZoom(1));
    terminal.getZoomOutButton().addActionListener(e -> applyZoom(-1));
    terminal.getDebugButton().addActionListener(e -> debugProject());
    terminal
        .getAddImportsButton()
        .addActionListener(
            e -> {
              saveSilently(); // keep current method edits before editing imports
              AddImportsWindow.launch(codingFile);
            });
    javax.swing.JToggleButton expert = terminal.getExpertToggle();
    expert.setSelected(codingFile.expertMode);
    expert.setText(codingFile.expertMode ? "Klassen: an" : "Klassen: aus");
    expert.addActionListener(
        e -> {
          boolean on = expert.isSelected();
          codeTabs.setExpertMode(on);
          expert.setText(on ? "Klassen: an" : "Klassen: aus");
        });
    // Persist whenever a method, field or class is added, renamed or deleted.
    codeTabs.setOnModelChanged(this::saveSilently);
    // Live syntax checking as the student types.
    codeTabs.setOnEdit(this::scheduleErrorCheck);
  }

  private void scheduleErrorCheck() {
    errorCheckTimer.restart();
  }

  /** Compiles in the background and marks syntax errors red in the editors. */
  private void checkErrors() {
    codeTabs.syncToModel();
    GeneratedProgram program = codingFile.buildProgram();
    codingFile.writeSources();
    java.util.List<String> paths = codingFile.javaFilePaths();
    String classpath = codingFile.generateClassPath();
    new Thread(
            () -> {
              JavaCompilerService.Result result = JavaCompilerService.compile(paths, classpath);
              EventQueue.invokeLater(() -> applyLiveErrors(program, result));
            })
        .start();
  }

  private void applyLiveErrors(GeneratedProgram program, JavaCompilerService.Result result) {
    codeTabs.clearAllErrors();
    java.util.List<ErrorListPanel.ErrorEntry> entries = new java.util.ArrayList<>();
    if (result.compilerAvailable()) {
      for (Diagnostic<? extends JavaFileObject> diagnostic : result.diagnostics()) {
        if (diagnostic.getKind() != Diagnostic.Kind.ERROR
            && diagnostic.getKind() != Diagnostic.Kind.WARNING) {
          continue;
        }
        String className = classNameOf(diagnostic);
        int line = (int) diagnostic.getLineNumber();
        if (className == null || line <= 0) {
          continue;
        }
        MethodLocation location = program.locate(className, line);
        if (location == null) {
          continue;
        }
        CodingArea area = codeTabs.getArea(location.classIndex(), location.methodIndex());
        if (area != null) {
          area.addError(location.localLine(), diagnostic.getMessage(null));
        }
        String where =
            location.methodIndex() >= 0
                ? "Klasse '"
                    + location.className()
                    + "' · Methode '"
                    + location.methodName()
                    + "', Zeile "
                    + location.localLine()
                : "Klasse '" + location.className() + "', Zeile " + location.localLine();
        entries.add(
            new ErrorListPanel.ErrorEntry(
                location.classIndex(),
                location.methodIndex(),
                location.localLine(),
                where + " — " + firstLine(diagnostic.getMessage(null))));
      }
    }
    codeTabs.applyAllErrors();
    errorListPanel.setErrors(entries);
  }

  private static String firstLine(String message) {
    if (message == null) {
      return "";
    }
    int newline = message.indexOf('\n');
    String line = newline >= 0 ? message.substring(0, newline) : message;
    return line.length() > 90 ? line.substring(0, 90) + "…" : line;
  }

  private void jumpToError(ErrorListPanel.ErrorEntry entry) {
    codeTabs.showError(entry.classIndex(), entry.methodIndex(), entry.localLine());
    CodingArea area = codeTabs.getArea(entry.classIndex(), entry.methodIndex());
    if (area != null) {
      area.getTextArea().requestFocusInWindow();
    }
  }

  private static String classNameOf(Diagnostic<? extends JavaFileObject> diagnostic) {
    JavaFileObject source = diagnostic.getSource();
    if (source == null) {
      return null;
    }
    String name = new File(source.getName()).getName();
    return name.endsWith(".java") ? name.substring(0, name.length() - ".java".length()) : name;
  }

  private void installShortcuts() {
    JRootPane root = frmSimplestJavaIDE.getRootPane();
    InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = root.getActionMap();
    int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask), "save");
    actionMap.put("save", action(this::saveProject));

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "run");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, menuMask), "run");
    actionMap.put("run", action(this::runProject));

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, menuMask), "zoomIn");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, menuMask), "zoomIn");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, menuMask), "zoomIn");
    actionMap.put("zoomIn", action(() -> applyZoom(1)));

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, menuMask), "zoomOut");
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, menuMask), "zoomOut");
    actionMap.put("zoomOut", action(() -> applyZoom(-1)));
  }

  /** Saves the project explicitly (with a confirmation message). */
  private void saveProject() {
    save(true);
  }

  private void saveSilently() {
    save(false);
  }

  private void save(boolean announce) {
    codeTabs.syncToModel();
    if (FileManager.save(codingFile)) {
      codingFile.writeSources();
      terminal.getSaveButton().setEnabled(false);
      if (announce) {
        Notifications.info("Gespeichert.");
      }
    }
  }

  private void autosave() {
    if (terminal.getSaveButton().isEnabled()) {
      saveSilently();
    }
  }

  private void runProject() {
    saveSilently();
    terminal.compile();
  }

  private void debugProject() {
    saveSilently();
    terminal.debug();
  }

  /** Opens the debugger panel for a fresh live session and steps the editor along. */
  private void openDebugPanel(DebugSession session, GeneratedProgram program) {
    debugPanel.begin(
        program,
        session,
        location -> {
          if (location != null) {
            codeTabs.showExecutionLine(
                location.classIndex(), location.methodIndex(), location.localLine());
          }
        });
    if (debugPanel.getParent() == null) {
      frmSimplestJavaIDE.getContentPane().add(debugPanel, BorderLayout.EAST);
      frmSimplestJavaIDE.getContentPane().revalidate();
      frmSimplestJavaIDE.getContentPane().repaint();
    }
  }

  private void closeDebug() {
    terminal.stopRunningProgram(); // stops the debug session if still running
    codeTabs.clearHighlights(); // also clears the execution highlight
    if (debugPanel.getParent() != null) {
      frmSimplestJavaIDE.getContentPane().remove(debugPanel);
      frmSimplestJavaIDE.getContentPane().revalidate();
      frmSimplestJavaIDE.getContentPane().repaint();
    }
  }

  /** Changes the editor and console font size by the given number of steps. */
  private void applyZoom(int steps) {
    int current = codeTabs.getReferenceFont().getSize();
    int newSize = Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, current + steps * 2));
    if (newSize == current) {
      return;
    }
    applyFontSize(newSize);
    AppPreferences.setFontSize(newSize);
  }

  private void applyFontSize(int size) {
    for (CodingArea area : codeTabs.getAllAreas()) {
      area.getTextArea().setFont(area.getTextArea().getFont().deriveFont((float) size));
    }
    terminal.getTextArea().setFont(terminal.getTextArea().getFont().deriveFont((float) size));
  }

  private void updateTitle() {
    boolean dirty = terminal.getSaveButton().isEnabled();
    frmSimplestJavaIDE.setTitle(
        (dirty ? "● " : "") + "Simplest Java IDE - " + codingFile.getFilepath());
    if (statusBar != null) {
      statusBar.setSaveState(dirty);
    }
  }

  /** Asks to save unsaved work, stops the running program, then exits. */
  private void handleClose() {
    if (terminal.getSaveButton().isEnabled()) { // there are unsaved changes
      int choice =
          JOptionPane.showConfirmDialog(
              frmSimplestJavaIDE,
              "Save changes before closing?",
              "Unsaved changes",
              JOptionPane.YES_NO_CANCEL_OPTION,
              JOptionPane.WARNING_MESSAGE);
      if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
        return; // keep the window open
      }
      if (choice == JOptionPane.YES_OPTION) {
        saveProject();
      }
    }
    AppPreferences.setWindowBounds(frmSimplestJavaIDE.getBounds());
    terminal.stopRunningProgram();
    deleteTempFiles();
    System.exit(0);
  }

  private void deleteTempFiles() {
    for (String path : codingFile.generatedFilePaths()) {
      deleteIfExists(path);
    }
  }

  private void deleteIfExists(String path) {
    File file = new File(path);
    if (file.exists() && !file.delete()) {
      ErrorPopupWindow.throwMessage("File " + file.getName() + " could not be deleted.");
    }
  }

  private void setWindowIcon() {
    try {
      frmSimplestJavaIDE.setIconImage(
          ImageIO.read(
              Objects.requireNonNull(getClass().getClassLoader().getResource("favicon.png"))));
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
  }

  private static Action action(Runnable runnable) {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        runnable.run();
      }
    };
  }
}
