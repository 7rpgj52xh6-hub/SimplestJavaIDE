package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.FileManager;

/**
 * Small dialog to add imports with less syntax (e.g. just {@code java.util.*}).
 * Changes are saved automatically.
 *
 * @author Daniel Trageser
 */
public class AddImportsWindow {

  private final CodingFile codingFile;
  private final DefaultListModel<String> listModel = new DefaultListModel<>();
  private JFrame frame;
  private JTextField importField;
  private JList<String> importList;

  public AddImportsWindow(CodingFile codingFile) {
    this.codingFile = codingFile;
    initialize();
  }

  /** Launch the window. */
  public static void launch(CodingFile codingFile) {
    EventQueue.invokeLater(
        () -> {
          try {
            new AddImportsWindow(codingFile).frame.setVisible(true);
          } catch (Exception e) {
            ErrorPopupWindow.throwMessage(e.getMessage());
          }
        });
  }

  private void initialize() {
    frame = new JFrame("Add imports — saved automatically");
    frame.setSize(560, 360);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            FileManager.save(codingFile);
          }
        });
    setWindowIcon();

    importField = new JTextField(24);
    importField.putClientProperty(
        "JTextField.placeholderText",
        "z. B.  import java.util.Scanner;   oder   java.util.Scanner");
    importField.addActionListener(e -> addImport(importField.getText()));
    JButton addButton = new JButton("Hinzufügen");
    addButton.setFocusable(false);
    addButton.addActionListener(e -> addImport(importField.getText()));

    JPanel inputRow = new JPanel(new BorderLayout(8, 0));
    inputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
    inputRow.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, importField.getPreferredSize().height + 4));
    inputRow.add(new JLabel("Import"), BorderLayout.WEST);
    inputRow.add(importField, BorderLayout.CENTER);
    inputRow.add(addButton, BorderLayout.EAST);

    JLabel helper =
        new JLabel("Ganze import-Zeile aus dem Internet einfügen oder nur das Paket — beides klappt.");
    helper.setForeground(new Color(0x9AA0A6));
    helper.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel quickLabel = new JLabel("Häufige Importe — ein Klick zeigt dir die Zeile:");
    quickLabel.setForeground(new Color(0x9AA0A6));
    quickLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel quickBar = new JPanel(new GridLayout(1, 0, 6, 0));
    quickBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    quickBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    addQuickImport(quickBar, "Scanner", "import java.util.Scanner;");
    addQuickImport(quickBar, "ArrayList", "import java.util.ArrayList;");
    addQuickImport(quickBar, "Arrays", "import java.util.Arrays;");
    addQuickImport(quickBar, "Random", "import java.util.Random;");
    addQuickImport(quickBar, "HashMap", "import java.util.HashMap;");

    JPanel north = new JPanel();
    north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
    north.setBorder(new EmptyBorder(12, 12, 8, 12));
    north.add(inputRow);
    north.add(Box.createVerticalStrut(6));
    north.add(helper);
    north.add(Box.createVerticalStrut(10));
    north.add(quickLabel);
    north.add(Box.createVerticalStrut(4));
    north.add(quickBar);
    frame.getContentPane().add(north, BorderLayout.NORTH);

    importList = new JList<>(listModel);
    JScrollPane scrollPane = new JScrollPane(importList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
    frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

    JButton deleteButton = new JButton("Delete");
    deleteButton.setPreferredSize(new Dimension(100, 32));
    deleteButton.addActionListener(e -> deleteSelected());
    JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
    southPanel.add(deleteButton);
    frame.getContentPane().add(southPanel, BorderLayout.SOUTH);

    loadImports();
  }

  /** A quick-pick button that shows the concrete import line in the field. */
  private void addQuickImport(JPanel bar, String label, String statement) {
    JButton button = new JButton(label);
    button.setFocusable(false);
    button.setToolTipText(statement);
    button.addActionListener(
        e -> {
          importField.setText(statement);
          importField.requestFocusInWindow();
        });
    bar.add(button);
  }

  private void addImport(String importString) {
    String value = normalize(importString);
    if (value.isEmpty() || codingFile.imports.contains(value)) {
      importField.setText("");
      return;
    }
    codingFile.imports.add(value);
    loadImports();
    FileManager.save(codingFile);
    importField.setText("");
  }

  /**
   * Accepts a full statement pasted from the web ("import java.util.*;") or just
   * the package ("java.util.*") and reduces both to the bare package.
   */
  private String normalize(String input) {
    if (input == null) {
      return "";
    }
    return input.trim().replaceFirst("(?i)^import\\s+", "").replaceFirst(";\\s*$", "").trim();
  }

  private void deleteSelected() {
    int index = importList.getSelectedIndex();
    if (index < 0) {
      ErrorPopupWindow.throwMessage("Select an import to delete first.");
      return;
    }
    codingFile.imports.remove(index);
    loadImports();
    FileManager.save(codingFile);
  }

  private void loadImports() {
    listModel.clear();
    for (String element : codingFile.imports) {
      listModel.addElement("import " + element + ";");
    }
  }

  private void setWindowIcon() {
    try {
      frame.setIconImage(
          ImageIO.read(
              Objects.requireNonNull(getClass().getClassLoader().getResource("favicon.png"))));
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
  }
}
