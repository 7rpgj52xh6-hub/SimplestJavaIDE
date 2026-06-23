package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
import simplestJavaIDEpackage.Icons;
import simplestJavaIDEpackage.Notifications;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.JavaClass;
import simplestJavaIDEpackage.Library.CodeStructure.JavaNames;
import simplestJavaIDEpackage.Library.CodeStructure.Methods;

/**
 * Editor for one class: each method is its own tab. In expert mode a leading
 * "Klasse" tab shows the class itself ({@code public class Name { ...attributes... }})
 * and is selected by default; the method tabs follow.
 *
 * @author Daniel Trageser
 */
public class ClassEditor extends JPanel {

  private static final long serialVersionUID = 11_000_000_001L;

  private final TerminalPanel terminal;
  private final Runnable onModelChanged;
  private final boolean withHeader;
  private final int headerOffset;
  private final JTabbedPane tabs;
  private final List<CodingArea> methodAreas = new ArrayList<>();
  private CodingArea headerArea;

  public ClassEditor(
      JavaClass javaClass, TerminalPanel terminal, Runnable onModelChanged, boolean withHeader) {
    super(new BorderLayout());
    this.terminal = terminal;
    this.onModelChanged = onModelChanged;
    this.withHeader = withHeader;
    this.headerOffset = withHeader ? 1 : 0;
    this.tabs = new JTabbedPane(SwingConstants.TOP);

    if (withHeader) {
      headerArea = createArea(new Methods("Klasse", javaClass.header), null);
      tabs.addTab(
          "Klasse",
          Icons.classMarker(),
          headerArea,
          "Die Klasse selbst: hier kommen Attribute und Konstruktor hin");
    }
    for (Methods method : javaClass.methods) {
      CodingArea area = createArea(method, referenceFont());
      methodAreas.add(area);
      tabs.addTab(method.name(), area);
      attachTitleRefresh(area);
    }
    addPlusTab();
    installContextMenu();
    tabs.setSelectedIndex(0);

    tabs.setBorder(BorderFactory.createEmptyBorder());
    add(tabs, BorderLayout.CENTER);
  }

  private Font referenceFont() {
    if (!methodAreas.isEmpty()) {
      return methodAreas.get(0).getTextArea().getFont();
    }
    return headerArea != null ? headerArea.getTextArea().getFont() : null;
  }

  private CodingArea createArea(Methods method, Font font) {
    return new CodingArea(method, terminal.getRunButton(), terminal.getSaveButton(), font);
  }

  private void addPlusTab() {
    tabs.addTab("", new JPanel());
    int plusIndex = tabs.getTabCount() - 1;
    JButton plusButton = new JButton("+");
    plusButton.setToolTipText("Neue Methode anlegen");
    plusButton.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
    plusButton.setFocusable(false);
    plusButton.addActionListener(e -> promptAddMethod());
    tabs.setTabComponentAt(plusIndex, plusButton);
    tabs.setEnabledAt(plusIndex, false);
  }

  private void installContextMenu() {
    tabs.addMouseListener(
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
    int methodIndex = tabs.indexAtLocation(e.getX(), e.getY()) - headerOffset;
    if (methodIndex < 0 || methodIndex >= methodAreas.size()) {
      return; // the "Klasse" tab or the "+" tab
    }
    JPopupMenu menu = new JPopupMenu();
    JMenuItem delete = new JMenuItem("Löschen");
    delete.setEnabled(methodIndex != 0); // the first method (with main) is protected
    delete.addActionListener(a -> deleteMethod(methodIndex));
    menu.add(delete);
    menu.show(tabs, e.getX(), e.getY());
  }

  private void promptAddMethod() {
    String name = JOptionPane.showInputDialog(this, "Name der Methode:", "Neue Methode");
    if (name == null) {
      return;
    }
    name = name.trim();
    if (!isValidNewName(name)) {
      return;
    }
    CodingArea area =
        createArea(new Methods(name, "public void " + name + "(){\n\t\n}"), referenceFont());
    int methodIndex = methodAreas.size();
    methodAreas.add(area);
    int tabIndex = methodIndex + headerOffset;
    tabs.insertTab(name, null, area, null, tabIndex);
    attachTitleRefresh(area);
    tabs.setSelectedIndex(tabIndex);
    onModelChanged.run();
  }

  /** Keeps a method tab's title in sync with the method name written in the code. */
  private void attachTitleRefresh(CodingArea area) {
    area.getTextArea()
        .addFocusListener(
            new FocusAdapter() {
              @Override
              public void focusLost(FocusEvent e) {
                refreshMethodTitle(area);
              }
            });
  }

  private void refreshMethodTitle(CodingArea area) {
    int methodIndex = methodAreas.indexOf(area);
    if (methodIndex < 0) {
      return;
    }
    String name = CodingFile.declaredMethodName(area.getTextArea().getText());
    if (name != null && !name.equals(area.getMethod().name())) {
      area.rename(name);
      tabs.setTitleAt(methodIndex + headerOffset, name);
    }
  }

  private void deleteMethod(int methodIndex) {
    if (methodIndex <= 0 || methodIndex >= methodAreas.size()) {
      return;
    }
    methodAreas.remove(methodIndex);
    tabs.removeTabAt(methodIndex + headerOffset);
    onModelChanged.run();
  }

  private boolean isValidNewName(String name) {
    String reason = JavaNames.invalidReason(name);
    if (reason != null) {
      Notifications.error("Methodenname: " + reason);
      return false;
    }
    for (CodingArea area : methodAreas) {
      if (area.getMethod().name().equals(name)) {
        Notifications.error("Eine Methode namens '" + name + "' gibt es schon.");
        return false;
      }
    }
    return true;
  }

  /** Writes the current methods (and header, if any) back into the class. */
  public void syncToModel(JavaClass target) {
    for (CodingArea area : methodAreas) {
      refreshMethodTitle(area);
    }
    target.methods.clear();
    for (CodingArea area : methodAreas) {
      target.methods.add(new Methods(area.getMethod().name(), area.getTextArea().getText()));
    }
    if (headerArea != null) {
      target.header = headerArea.getTextArea().getText();
    }
  }

  public List<CodingArea> getAllAreas() {
    List<CodingArea> all = new ArrayList<>();
    if (headerArea != null) {
      all.add(headerArea);
    }
    all.addAll(methodAreas);
    return all;
  }

  public CodingArea getReferenceArea() {
    if (!methodAreas.isEmpty()) {
      return methodAreas.get(0);
    }
    return headerArea;
  }

  /** The editor for a method ({@code methodIndex >= 0}) or the class header ({@code -1}). */
  public CodingArea getArea(int methodIndex) {
    if (methodIndex < 0) {
      return headerArea;
    }
    return methodIndex < methodAreas.size() ? methodAreas.get(methodIndex) : null;
  }

  /** {@code methodIndex == -1} targets the class header tab. */
  public void showError(int methodIndex, int localLine) {
    CodingArea area = areaFor(methodIndex);
    if (area != null) {
      area.highlightErrorLine(localLine);
    }
  }

  public void showExecutionLine(int methodIndex, int localLine) {
    CodingArea area = areaFor(methodIndex);
    if (area != null) {
      area.highlightExecutionLine(localLine);
    }
  }

  private CodingArea areaFor(int methodIndex) {
    if (methodIndex < 0) {
      if (headerArea != null) {
        tabs.setSelectedIndex(0);
      }
      return headerArea;
    }
    if (methodIndex >= methodAreas.size()) {
      return null;
    }
    tabs.setSelectedIndex(methodIndex + headerOffset);
    return methodAreas.get(methodIndex);
  }

  public void clearHighlights() {
    for (CodingArea area : getAllAreas()) {
      area.clearErrorHighlights();
    }
  }
}
