package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
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
    importField.setToolTipText(
        "Type an import without the surrounding syntax, e.g. java.util.* — then press Enter");
    importField.addActionListener(e -> addImport(importField.getText()));
    JButton addButton = new JButton("Add");
    addButton.addActionListener(e -> addImport(importField.getText()));

    JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
    inputPanel.add(new JLabel("import"));
    inputPanel.add(importField);
    inputPanel.add(new JLabel(";"));
    inputPanel.add(addButton);
    frame.getContentPane().add(inputPanel, BorderLayout.NORTH);

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

  private void addImport(String importString) {
    String value = importString == null ? "" : importString.trim();
    if (value.isEmpty() || codingFile.imports.contains(value)) {
      importField.setText("");
      return;
    }
    codingFile.imports.add(value);
    loadImports();
    FileManager.save(codingFile);
    importField.setText("");
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
