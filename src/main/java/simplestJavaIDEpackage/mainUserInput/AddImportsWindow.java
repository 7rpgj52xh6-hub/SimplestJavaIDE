package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.FileManager;

public class AddImportsWindow {

  private final CodingFile codingFile;
  private final DefaultListModel<String> listModel = new DefaultListModel<>();
  private final WindowListener saveOnClosing =
      new WindowListener() {

        @Override
        public void windowOpened(WindowEvent e) {
          loadImports();
        }

        @Override
        public void windowIconified(WindowEvent e) {}

        @Override
        public void windowDeiconified(WindowEvent e) {}

        @Override
        public void windowDeactivated(WindowEvent e) {
          FileManager.save(codingFile);
        }

        @Override
        public void windowClosing(WindowEvent e) {
          FileManager.save(codingFile);
        }

        @Override
        public void windowClosed(WindowEvent e) {
          FileManager.save(codingFile);
        }

        @Override
        public void windowActivated(WindowEvent e) {
          loadImports();
        }
      };
  private JFrame frmImportWindow;
  private JTextField textfieldImports;

  /** Create the application. */
  public AddImportsWindow(CodingFile codingFile) {
    this.codingFile = codingFile;
    initialize();
  }

  /** Launch the application. */
  public static void launch(CodingFile codingFile) {
    EventQueue.invokeLater(
            () -> {
              try {
                AddImportsWindow window = new AddImportsWindow(codingFile);
                window.frmImportWindow.setVisible(true);
              } catch (Exception e) {
                ErrorPopupWindow.throwMessage(e.getMessage());
              }
            });
  }

  /** Initialize the contents of the frame. */
  private void initialize() {

    frmImportWindow = new JFrame("[Saved automatically] - Add imports with less syntax'");
    frmImportWindow.setBounds(100, 100, 640, 360);
    frmImportWindow.addWindowListener(saveOnClosing);
    frmImportWindow.setResizable(false);

    // Set Icon
    try {
      frmImportWindow.setIconImage(
          ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("favicon.png"))));
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }

    JSplitPane splitPane = new JSplitPane();
    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    frmImportWindow.getContentPane().add(splitPane, BorderLayout.CENTER);

    JPanel inputPanel = new JPanel();
    splitPane.setLeftComponent(inputPanel);

    JLabel labelImport = new JLabel("import");
    inputPanel.add(labelImport);

    textfieldImports = new JTextField();
    textfieldImports.setHorizontalAlignment(SwingConstants.CENTER);
    textfieldImports.setToolTipText(
        "Input your desired import without most of the syntax. For example only \"java.util.*\" is needed.");
    inputPanel.add(textfieldImports);
    textfieldImports.setColumns(25);

    JLabel labelSemicolon = new JLabel(";");
    inputPanel.add(labelSemicolon);

    JList<String> listOfImports = new JList<>(listModel);
    splitPane.setRightComponent(listOfImports);

    JButton btnAddImport = new JButton("Add");
    btnAddImport.addActionListener(
            arg0 -> {
              addImport(textfieldImports.getText());
              textfieldImports.setText("");
            });
    btnAddImport.setPreferredSize(new Dimension(100, 36));
    inputPanel.add(btnAddImport);

    JButton btnDeleteImport = new JButton("Delete");
    btnDeleteImport.addActionListener(
            arg0 -> {
              int toDeleteIndex = listOfImports.getSelectedIndex();
              listModel.remove(toDeleteIndex);
              codingFile.imports.remove(toDeleteIndex);
            });
    btnDeleteImport.setPreferredSize(new Dimension(100, 36));
    inputPanel.add(btnDeleteImport);
  }

  private String castToFullImport(String line) {
    return "import " + line + ";";
  }

  public void loadImports() {
    listModel.clear();
    for (String element : codingFile.imports) {
      listModel.addElement(castToFullImport(element));
    }
  }

  public void addImport(String importString) {
    if (!codingFile.imports.contains(importString)) {
      codingFile.imports.add(importString);
      listModel.clear();
      for (String i : codingFile.imports) {
        listModel.addElement(castToFullImport(i));
      }
      FileManager.save(codingFile);
    }
  }
}
