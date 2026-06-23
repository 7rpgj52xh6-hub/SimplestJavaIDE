package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import simplestJavaIDEpackage.Notifications;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.Methods;

/**
 * Holds one editor tab per method. Methods are added with the "+" tab and
 * renamed or deleted via a right-click context menu — no separate management
 * tab. Compiler errors are routed here to highlight the offending line.
 *
 * @author Daniel Trageser
 */
public class MethodTabsPanel extends JPanel {

  private static final long serialVersionUID = 1230287069025734089L;

  private final CodingFile codingFile;
  private final TerminalPanel terminal;
  private final JTabbedPane tabbedPaneMethods;
  private final CodingArea mainCodingArea;
  private final List<CodingArea> listOfCodingAreas = new ArrayList<>();

  private Runnable onModelChanged = () -> {};

  public MethodTabsPanel(CodingFile codingFile, TerminalPanel terminal) {
    super(new BorderLayout());
    this.codingFile = codingFile;
    this.terminal = terminal;
    // Let the terminal route compiler errors back to the matching method tab.
    terminal.setMethodTabsPanel(this);

    tabbedPaneMethods = new JTabbedPane(SwingConstants.TOP);

    mainCodingArea = createCodingArea(codingFile.methods.get(0), null);
    listOfCodingAreas.add(mainCodingArea);
    tabbedPaneMethods.addTab(codingFile.methods.get(0).name(), mainCodingArea);
    for (int i = 1; i < codingFile.methods.size(); i++) {
      CodingArea area = createCodingArea(codingFile.methods.get(i), mainCodingArea.getFont());
      listOfCodingAreas.add(area);
      tabbedPaneMethods.addTab(codingFile.methods.get(i).name(), area);
    }

    addPlusTab();
    installContextMenu();

    tabbedPaneMethods.setBorder(BorderFactory.createEmptyBorder());
    add(tabbedPaneMethods, BorderLayout.CENTER);
  }

  private CodingArea createCodingArea(Methods method, java.awt.Font font) {
    return new CodingArea(method, terminal.getRunButton(), terminal.getSaveButton(), font);
  }

  /** Adds the trailing "+" tab whose tab component is a button that adds a method. */
  private void addPlusTab() {
    tabbedPaneMethods.addTab("", new JPanel());
    int plusIndex = tabbedPaneMethods.getTabCount() - 1;
    JButton plusButton = new JButton("+");
    plusButton.setToolTipText("Neue Methode anlegen");
    plusButton.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
    plusButton.setFocusable(false);
    plusButton.addActionListener(e -> promptAddMethod());
    tabbedPaneMethods.setTabComponentAt(plusIndex, plusButton);
    tabbedPaneMethods.setEnabledAt(plusIndex, false);
  }

  private void installContextMenu() {
    tabbedPaneMethods.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            maybeShowMenu(e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            maybeShowMenu(e);
          }
        });
  }

  private void maybeShowMenu(MouseEvent e) {
    if (!e.isPopupTrigger()) {
      return;
    }
    int index = tabbedPaneMethods.indexAtLocation(e.getX(), e.getY());
    if (index < 0 || index >= listOfCodingAreas.size()) {
      return; // not a real method tab (e.g. the "+" tab)
    }
    JPopupMenu menu = new JPopupMenu();
    JMenuItem rename = new JMenuItem("Rename…");
    rename.addActionListener(a -> promptRenameMethod(index));
    menu.add(rename);
    JMenuItem delete = new JMenuItem("Delete");
    delete.setEnabled(index != 0); // the main method cannot be deleted
    delete.addActionListener(a -> deleteMethod(index));
    menu.add(delete);
    menu.show(tabbedPaneMethods, e.getX(), e.getY());
  }

  private void promptAddMethod() {
    String name = JOptionPane.showInputDialog(this, "Method name:", "New method");
    if (name == null) {
      return; // cancelled
    }
    name = name.trim();
    if (!isValidNewName(name)) {
      return;
    }
    CodingArea area =
        createCodingArea(
            new Methods(name, "public static void " + name + "(){\n\t\n}"),
            mainCodingArea.getTextArea().getFont());
    int index = listOfCodingAreas.size();
    listOfCodingAreas.add(area);
    tabbedPaneMethods.insertTab(name, null, area, null, index);
    tabbedPaneMethods.setSelectedIndex(index);
    onModelChanged.run();
  }

  private void promptRenameMethod(int index) {
    CodingArea area = listOfCodingAreas.get(index);
    String name =
        (String)
            JOptionPane.showInputDialog(
                this,
                "Method name:",
                "Rename method",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                area.getMethod().name());
    if (name == null) {
      return;
    }
    name = name.trim();
    if (name.equals(area.getMethod().name())) {
      return;
    }
    if (!isValidNewName(name)) {
      return;
    }
    area.rename(name);
    tabbedPaneMethods.setTitleAt(index, name);
    onModelChanged.run();
  }

  private void deleteMethod(int index) {
    if (index <= 0 || index >= listOfCodingAreas.size()) {
      return;
    }
    listOfCodingAreas.remove(index);
    tabbedPaneMethods.removeTabAt(index);
    onModelChanged.run();
  }

  private boolean isValidNewName(String name) {
    if (name.isEmpty()) {
      Notifications.error("Method name must not be empty.");
      return false;
    }
    if (name.contains(" ")) {
      Notifications.error("Method name must not contain spaces.");
      return false;
    }
    for (CodingArea area : listOfCodingAreas) {
      if (area.getMethod().name().equals(name)) {
        Notifications.error("A method named '" + name + "' already exists.");
        return false;
      }
    }
    return true;
  }

  /** Rebuilds the saved model from the current tabs (names + editor contents). */
  public void syncMethodsToModel() {
    codingFile.methods.clear();
    for (CodingArea area : listOfCodingAreas) {
      codingFile.methods.add(new Methods(area.getMethod().name(), area.getTextArea().getText()));
    }
  }

  public void setOnModelChanged(Runnable onModelChanged) {
    this.onModelChanged = onModelChanged;
  }

  public CodingArea getMainCodingArea() {
    return mainCodingArea;
  }

  public List<CodingArea> getListOfCodingAreas() {
    return listOfCodingAreas;
  }

  /** Selects the tab of the given method and highlights the offending line. */
  public void showError(int methodIndex, int localLine) {
    if (methodIndex < 0 || methodIndex >= listOfCodingAreas.size()) {
      return;
    }
    tabbedPaneMethods.setSelectedIndex(methodIndex);
    listOfCodingAreas.get(methodIndex).highlightErrorLine(localLine);
  }

  public void clearErrorHighlights() {
    for (CodingArea area : listOfCodingAreas) {
      area.clearErrorHighlights();
    }
  }

  /** Selects the tab of the given method and marks the currently executing line. */
  public void showExecutionLine(int methodIndex, int localLine) {
    if (methodIndex < 0 || methodIndex >= listOfCodingAreas.size()) {
      return;
    }
    tabbedPaneMethods.setSelectedIndex(methodIndex);
    listOfCodingAreas.get(methodIndex).highlightExecutionLine(localLine);
  }
}
