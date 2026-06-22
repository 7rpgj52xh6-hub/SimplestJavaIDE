package simplestJavaIDEpackage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
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
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import simplestJavaIDEpackage.Library.AddImportsWindow;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.FileManager;
import simplestJavaIDEpackage.Library.CodingArea;
import simplestJavaIDEpackage.Library.MethodTabsPanel;
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
  private MethodTabsPanel methodTabsPanel;

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
            window.frmSimplestJavaIDE.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
          } catch (Exception e) {
            ErrorPopupWindow.throwMessage(e.getMessage());
          }
        });
  }

  /** Initialize the contents of the frame. */
  private void initialize() {
    frmSimplestJavaIDE = new JFrame();
    frmSimplestJavaIDE.setSize(1080, 720);
    frmSimplestJavaIDE.setMinimumSize(new Dimension(900, 600));
    frmSimplestJavaIDE.getContentPane().setLayout(new BorderLayout(0, 0));
    frmSimplestJavaIDE.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            deleteTempFiles();
          }
        });

    setWindowIcon();

    // Structure of main window: methods on top, console below.
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    frmSimplestJavaIDE.getContentPane().add(mainSplitPane, BorderLayout.CENTER);
    mainSplitPane.setResizeWeight(0.8);
    mainSplitPane.setDividerLocation(frmSimplestJavaIDE.getHeight() - 300);

    terminal = new TerminalPanel(codingFile);
    methodTabsPanel = new MethodTabsPanel(codingFile, terminal);
    mainSplitPane.setTopComponent(methodTabsPanel);
    mainSplitPane.setBottomComponent(terminal);

    StatusBar statusBar = new StatusBar();
    frmSimplestJavaIDE.getContentPane().add(statusBar, BorderLayout.SOUTH);
    Notifications.setStatusBar(statusBar);

    wireActions();
    installShortcuts();

    // A freshly loaded file is not "unsaved" even though setting the editor text
    // flips the save button on during construction.
    terminal.getSaveButton().setEnabled(false);
    terminal.getSaveButton().addPropertyChangeListener("enabled", e -> updateTitle());
    updateTitle();
  }

  private void wireActions() {
    terminal.getHelpButton().addActionListener(e -> ImprintWindow.main(null));
    terminal.getSaveButton().addActionListener(e -> saveProject());
    terminal.getRunButton().addActionListener(e -> runProject());
    terminal.getZoomInButton().addActionListener(e -> applyZoom(1));
    terminal.getZoomOutButton().addActionListener(e -> applyZoom(-1));
    terminal
        .getAddImportsButton()
        .addActionListener(
            e -> {
              saveProject(); // keep current method edits before editing imports
              AddImportsWindow.launch(codingFile);
            });
    // Persist whenever a method is added, renamed or deleted.
    methodTabsPanel.setOnModelChanged(this::saveProject);
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

  /** Saves the project: rebuild the model from the tabs, write it, refresh state. */
  private void saveProject() {
    methodTabsPanel.syncMethodsToModel();
    if (FileManager.save(codingFile)) {
      codingFile.tmpSaveAndRunJavaCode();
      terminal.getSaveButton().setEnabled(false);
      Notifications.info("Saved.");
    }
  }

  private void runProject() {
    saveProject();
    terminal.compile();
  }

  /** Changes the editor and console font size by the given number of steps. */
  private void applyZoom(int steps) {
    Font base = methodTabsPanel.getMainCodingArea().getTextArea().getFont();
    int newSize = Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, base.getSize() + steps * 2));
    if (newSize == base.getSize()) {
      return;
    }
    for (CodingArea area : methodTabsPanel.getListOfCodingAreas()) {
      area.getTextArea().setFont(area.getTextArea().getFont().deriveFont((float) newSize));
    }
    terminal.getTextArea().setFont(terminal.getTextArea().getFont().deriveFont((float) newSize));
  }

  private void updateTitle() {
    boolean dirty = terminal.getSaveButton().isEnabled();
    frmSimplestJavaIDE.setTitle(
        (dirty ? "● " : "") + "Simplest Java IDE - " + codingFile.getFilepath());
  }

  private void deleteTempFiles() {
    deleteIfExists(codingFile.getJavaTmpFilePath());
    deleteIfExists(codingFile.getJavaTmpClassPath());
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
