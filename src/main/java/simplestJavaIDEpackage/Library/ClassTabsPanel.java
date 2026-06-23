package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Font;
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
import simplestJavaIDEpackage.Library.CodeStructure.JavaClass;
import simplestJavaIDEpackage.Library.CodeStructure.JavaNames;

/**
 * The top editing area. Default mode shows a single class's methods. Expert mode
 * adds a row of class tabs; each class is a {@link ClassEditor} with a leading
 * "Klasse" tab (the class itself) plus its method tabs.
 *
 * @author Daniel Trageser
 */
public class ClassTabsPanel extends JPanel {

  private static final long serialVersionUID = 11_000_000_002L;

  private final CodingFile codingFile;
  private final TerminalPanel terminal;
  private final List<ClassEditor> classEditors = new ArrayList<>();
  private Runnable onModelChanged = () -> {};
  private Runnable onEdit = () -> {};
  private JTabbedPane classTabs;

  public ClassTabsPanel(CodingFile codingFile, TerminalPanel terminal) {
    super(new BorderLayout());
    this.codingFile = codingFile;
    this.terminal = terminal;
    terminal.setCodeTabs(this);
    build();
  }

  public void setOnModelChanged(Runnable onModelChanged) {
    this.onModelChanged = onModelChanged;
  }

  public void setOnEdit(Runnable onEdit) {
    this.onEdit = onEdit;
  }

  private void build() {
    removeAll();
    classEditors.clear();
    classTabs = null;

    if (!codingFile.expertMode) {
      ClassEditor editor =
          new ClassEditor(codingFile.classes.get(0), terminal, () -> onModelChanged.run(), false);
      classEditors.add(editor);
      add(editor, BorderLayout.CENTER);
    } else {
      classTabs = new JTabbedPane(SwingConstants.TOP);
      for (JavaClass javaClass : codingFile.classes) {
        seedHeader(javaClass);
        ClassEditor editor =
            new ClassEditor(javaClass, terminal, () -> onModelChanged.run(), true);
        classEditors.add(editor);
        classTabs.addTab(javaClass.name, editor);
      }
      addPlusTab();
      installClassContextMenu();
      add(classTabs, BorderLayout.CENTER);
    }
    for (ClassEditor editor : classEditors) {
      for (CodingArea area : editor.getAllAreas()) {
        area.setOnEdit(() -> onEdit.run());
      }
    }
    revalidate();
    repaint();
  }

  private void seedHeader(JavaClass javaClass) {
    if (javaClass.header == null || javaClass.header.isBlank()) {
      javaClass.header =
          "public class "
              + javaClass.name
              + " {\n\n\t// Attribute hier, z. B.:  private int wert;\n\n}";
    }
  }

  private void addPlusTab() {
    classTabs.addTab("", new JPanel());
    int plusIndex = classTabs.getTabCount() - 1;
    JButton plusButton = new JButton("+ Klasse");
    plusButton.setToolTipText("Neue Klasse anlegen");
    plusButton.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
    plusButton.setFocusable(false);
    plusButton.addActionListener(e -> promptAddClass());
    classTabs.setTabComponentAt(plusIndex, plusButton);
    classTabs.setEnabledAt(plusIndex, false);
  }

  private void installClassContextMenu() {
    classTabs.addMouseListener(
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
    int index = classTabs.indexAtLocation(e.getX(), e.getY());
    if (index < 0 || index >= classEditors.size()) {
      return;
    }
    JPopupMenu menu = new JPopupMenu();
    JMenuItem delete = new JMenuItem("Klasse löschen");
    delete.setEnabled(index != 0 && classEditors.size() > 1); // first class = project's main class
    delete.addActionListener(a -> deleteClass(index));
    menu.add(delete);
    menu.show(classTabs, e.getX(), e.getY());
  }

  private void promptAddClass() {
    String name = JOptionPane.showInputDialog(this, "Name der Klasse:", "Neue Klasse");
    if (name == null) {
      return;
    }
    name = name.trim();
    if (!isValidClassName(name)) {
      return;
    }
    syncToModel();
    codingFile.classes.add(new JavaClass(name));
    build();
    classTabs.setSelectedIndex(classEditors.size() - 1);
    onModelChanged.run();
  }

  private void deleteClass(int index) {
    if (index <= 0 || index >= classEditors.size() || classEditors.size() <= 1) {
      return;
    }
    syncToModel();
    codingFile.classes.remove(index);
    build();
    onModelChanged.run();
  }

  private boolean isValidClassName(String name) {
    String reason = JavaNames.invalidReason(name);
    if (reason != null) {
      Notifications.error("Klassenname: " + reason);
      return false;
    }
    for (JavaClass javaClass : codingFile.classes) {
      if (javaClass.name.equals(name)) {
        Notifications.error("Eine Klasse namens '" + name + "' gibt es schon.");
        return false;
      }
    }
    return true;
  }

  public void setExpertMode(boolean expert) {
    if (codingFile.expertMode == expert) {
      return;
    }
    syncToModel();
    codingFile.expertMode = expert;
    build();
    onModelChanged.run();
  }

  /** Writes every class editor's contents back into the model. */
  public void syncToModel() {
    for (int i = 0; i < classEditors.size() && i < codingFile.classes.size(); i++) {
      JavaClass javaClass = codingFile.classes.get(i);
      classEditors.get(i).syncToModel(javaClass);
      // Keep the class tab title in sync with the "public class ..." in the code.
      if (classTabs != null) {
        String name = CodingFile.declaredClassName(javaClass.header);
        if (name != null) {
          javaClass.name = name;
          classTabs.setTitleAt(i, name);
        }
      }
    }
  }

  public void clearHighlights() {
    for (ClassEditor editor : classEditors) {
      editor.clearHighlights();
    }
  }

  public void showError(int classIndex, int methodIndex, int localLine) {
    selectClass(classIndex);
    if (classIndex >= 0 && classIndex < classEditors.size()) {
      classEditors.get(classIndex).showError(methodIndex, localLine);
    }
  }

  public void showExecutionLine(int classIndex, int methodIndex, int localLine) {
    selectClass(classIndex);
    if (classIndex >= 0 && classIndex < classEditors.size()) {
      classEditors.get(classIndex).showExecutionLine(methodIndex, localLine);
    }
  }

  private void selectClass(int classIndex) {
    if (classTabs != null && classIndex >= 0 && classIndex < classEditors.size()) {
      classTabs.setSelectedIndex(classIndex);
    }
  }

  public List<CodingArea> getAllAreas() {
    List<CodingArea> all = new ArrayList<>();
    for (ClassEditor editor : classEditors) {
      all.addAll(editor.getAllAreas());
    }
    return all;
  }

  /** The editor for a (class, method) location ({@code methodIndex == -1} = class header). */
  public CodingArea getArea(int classIndex, int methodIndex) {
    if (classIndex < 0 || classIndex >= classEditors.size()) {
      return null;
    }
    return classEditors.get(classIndex).getArea(methodIndex);
  }

  public void clearAllErrors() {
    for (CodingArea area : getAllAreas()) {
      area.clearErrors();
    }
  }

  public void applyAllErrors() {
    for (CodingArea area : getAllAreas()) {
      area.applyErrors();
    }
  }

  public Font getReferenceFont() {
    CodingArea reference = classEditors.get(0).getReferenceArea();
    return reference != null ? reference.getTextArea().getFont() : getFont();
  }
}
