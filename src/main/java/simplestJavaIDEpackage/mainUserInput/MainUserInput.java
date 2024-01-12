package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.ImprintWindow;
import simplestJavaIDEpackage.Library.CodingArea;
import simplestJavaIDEpackage.Library.FileManager;
import simplestJavaIDEpackage.Library.Methods;
import simplestJavaIDEpackage.Library.TerminalPanel;

/**
 * @author Daniel Trageser This class implements the main user interface and its functions
 */
public class MainUserInput {

  private final List<CodingArea> listOfCodingAreas = new ArrayList<>();
  private final DefaultListModel<String> listModel = new DefaultListModel<>();
  private JFrame frmSimplestJavaIDE;
  private TerminalPanel terminal;
  private JTabbedPane tabbedPaneMethods;

  /** Create the application. */
  public MainUserInput(CodingFile savefile) {
    initialize(savefile);
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
  private void initialize(CodingFile codingFile) {
    // Main Window
    frmSimplestJavaIDE = new JFrame("Simplest Java IDE - " + codingFile.getFilepath());
    frmSimplestJavaIDE.setSize(1080, 720);
    frmSimplestJavaIDE.setMinimumSize(new Dimension(1080, 720));
    frmSimplestJavaIDE.getContentPane().setLayout(new BorderLayout(0, 0));
    frmSimplestJavaIDE.addWindowListener(
        new WindowListener() {

          @Override
          public void windowClosing(WindowEvent arg0) {
            File javaFile = new File(codingFile.getJavaTmpFilePath());
            if (javaFile.exists()) {
              if (!javaFile.delete())
                ErrorPopupWindow.throwMessage(
                    "File " + javaFile.getName() + " could not be deleted.");
            }
            File classFile = new File(codingFile.getJavaTmpClassPath());
            if (classFile.exists()) {
              if (!classFile.delete())
                ErrorPopupWindow.throwMessage(
                    "File " + classFile.getName() + " could not be deleted.");
            }
          }

          @Override
          public void windowActivated(WindowEvent e) {
            // Do nothing

          }

          @Override
          public void windowClosed(WindowEvent e) {
            // Do nothing

          }

          @Override
          public void windowDeactivated(WindowEvent e) {
            // Do nothing

          }

          @Override
          public void windowDeiconified(WindowEvent e) {
            // Do nothing

          }

          @Override
          public void windowIconified(WindowEvent e) {
            // Do nothing

          }

          @Override
          public void windowOpened(WindowEvent e) {
            // Do nothing

          }
        });

    // Set Icon
    try {
      frmSimplestJavaIDE.setIconImage(
          ImageIO.read(
              Objects.requireNonNull(getClass().getClassLoader().getResource("favicon.png"))));
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }

    // Structure of main window
    JSplitPane MainUserInputSplitPane = new JSplitPane();
    MainUserInputSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    frmSimplestJavaIDE.getContentPane().add(MainUserInputSplitPane, BorderLayout.CENTER);
    MainUserInputSplitPane.setResizeWeight(0.9);
    MainUserInputSplitPane.setDividerLocation(frmSimplestJavaIDE.getHeight() - 300);
    MainUserInputSplitPane.getBottomComponent().setMinimumSize(new Dimension(20000, 286));

    // Output
    JPanel bottomPanel = new JPanel();
    MainUserInputSplitPane.setBottomComponent(bottomPanel);
    bottomPanel.setPreferredSize(new Dimension(200, 286));
    bottomPanel.setLayout(new BorderLayout(0, 0));
    terminal = new TerminalPanel(codingFile);
    bottomPanel.add(terminal);

    // Coding input and load code if code is not null (from loading file)
    CodingArea mainCodingArea =
        new CodingArea(
            codingFile.methods.get(0), terminal.getRunButton(), terminal.getSaveButton(), null);

    tabbedPaneMethods = new JTabbedPane(SwingConstants.TOP);
    MainUserInputSplitPane.setLeftComponent(tabbedPaneMethods);
    listOfCodingAreas.add(mainCodingArea);
    for (int i = 1; i < codingFile.methods.size(); i++) {
      listOfCodingAreas.add(
          new CodingArea(
              codingFile.methods.get(i),
              terminal.getRunButton(),
              terminal.getSaveButton(),
              mainCodingArea.getFont()));
    }
    // Add a Tab for every coding area
    for (CodingArea i : listOfCodingAreas) {
      int index = tabbedPaneMethods.getTabCount();
      tabbedPaneMethods.insertTab(i.getMethod().getName(), null, i, null, index);
    }

    // Panel for managing methods
    JSplitPane splitPaneManagingMethods = new JSplitPane();
    splitPaneManagingMethods.setOrientation(JSplitPane.VERTICAL_SPLIT);
    JPanel inputPanel = new JPanel();
    splitPaneManagingMethods.setLeftComponent(inputPanel);

    JLabel labelImport = new JLabel("Method name:");
    inputPanel.add(labelImport);

    JTextField textFieldMethodName = new JTextField();
    textFieldMethodName.setHorizontalAlignment(SwingConstants.CENTER);
    textFieldMethodName.setText("");
    inputPanel.add(textFieldMethodName);
    textFieldMethodName.setColumns(25);

    JList<String> listOfMethods = new JList<>(listModel);
    for (Methods i : codingFile.methods) {
      listModel.add(codingFile.methods.indexOf(i), i.getName());
    }
    splitPaneManagingMethods.setRightComponent(listOfMethods);

    JButton btnAddMethod = getBtnAddMethod(codingFile, textFieldMethodName, mainCodingArea);
    inputPanel.add(btnAddMethod);

    JButton btnDeleteMethod = getBtnDeleteMethod(codingFile, listOfMethods);
    inputPanel.add(btnDeleteMethod);

    JLabel labelManagingMethods = new JLabel("⚙");
    Font currentFont = labelManagingMethods.getFont();
    Font newFont = currentFont.deriveFont(currentFont.getSize() * 2F);
    labelManagingMethods.setFont(newFont);
    tabbedPaneMethods.addTab("[Managing Methods]", splitPaneManagingMethods); // TODO design Add
    // panel
    int indexOfManagingMethodsTab = -1;
    for (int i = 1; i < tabbedPaneMethods.getTabCount(); i++) {
      if (tabbedPaneMethods.getTitleAt(i).equals("[Managing Methods]")) {
        indexOfManagingMethodsTab = i;
      }
    }
    if (indexOfManagingMethodsTab != -1) {
      tabbedPaneMethods.setTabComponentAt(indexOfManagingMethodsTab, labelManagingMethods);
    }
    tabbedPaneMethods.setBorder(BorderFactory.createLineBorder(new Color(50, 53, 55)));
    tabbedPaneMethods.setBackground(new Color(55, 58, 60));

    // Action button interactions
    terminal.getHelpButton().addActionListener(e -> ImprintWindow.main(null));
    terminal
        .getSaveButton()
        .addActionListener(
            e -> {
              for (CodingArea i : listOfCodingAreas) {
                codingFile.methods.set(
                    listOfCodingAreas.indexOf(i),
                    new Methods(i.getMethod().getName(), i.getTextArea().getText()));
              }
              if (FileManager.save(codingFile)) {
                codingFile.tmpSaveAndRunJavaCode();
                terminal.getSaveButton().setEnabled(false);
              }
            });
    terminal
        .getRunButton()
        .addActionListener(
            e -> {
              // Save, compile and run
              for (CodingArea i : listOfCodingAreas) {
                codingFile.methods.set(
                    listOfCodingAreas.indexOf(i),
                    new Methods(i.getMethod().getName(), i.getTextArea().getText()));
              }
              if (FileManager.save(codingFile)) {
                codingFile.tmpSaveAndRunJavaCode();
                terminal.getSaveButton().setEnabled(false);
              }
              terminal.compile();
            });
    terminal
        .getZoomInButton()
        .getModel()
        .addChangeListener(
            new ChangeListener() {
              private final Timer trigger = new Timer(125, e -> zoomIn());

              public void zoomIn() {
                // Add Zoom
                Font font = mainCodingArea.getTextArea().getFont();
                if (font.getSize() <= 60) {
                  for (CodingArea i : listOfCodingAreas) {
                    i.getTextArea()
                        .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
                  }
                  terminal
                      .getTextArea()
                      .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
                  terminal.getZoomOutButton().setEnabled(true);
                }
                if (font.getSize() >= 58) {
                  terminal.getZoomInButton().setEnabled(false);
                }
              }

              @Override
              public void stateChanged(ChangeEvent e) {
                if (terminal.getZoomInButton().getModel().isPressed()) {
                  zoomIn();
                  trigger.start();
                } else {
                  trigger.stop();
                }
              }
            });
    terminal
        .getZoomOutButton()
        .getModel()
        .addChangeListener(
            new ChangeListener() {
              private final Timer trigger = new Timer(125, e -> zoomOut());

              public void zoomOut() {
                // Subtract Zoom
                Font font = mainCodingArea.getTextArea().getFont();
                if (font.getSize() >= 10) {
                  for (CodingArea i : listOfCodingAreas) {
                    i.getTextArea()
                        .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
                  }
                  terminal
                      .getTextArea()
                      .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
                  terminal.getZoomInButton().setEnabled(true);
                }
                if (font.getSize() <= 12) {
                  terminal.getZoomOutButton().setEnabled(false);
                }
              }

              @Override
              public void stateChanged(ChangeEvent e) {
                if (terminal.getZoomOutButton().getModel().isPressed()) {
                  zoomOut();
                  trigger.start();
                } else {
                  trigger.stop();
                }
              }
            });
    terminal
        .getAddImportsButton()
        .addActionListener(
            e -> {
              AddImportsWindow.launch(codingFile);
              FileManager.save(codingFile); // Also save other code
            });
  }

  private JButton getBtnDeleteMethod(CodingFile codingFile, JList<String> listOfMethods) {
    JButton btnDeleteMethod = new JButton("Delete");
    btnDeleteMethod.addActionListener(
        arg0 -> {
          int toDeleteIndex = listOfMethods.getSelectedIndex();
          if (toDeleteIndex != 0) {
            listModel.remove(toDeleteIndex); // remove listModel entry
            codingFile.methods.remove(toDeleteIndex); // remove method
            listOfCodingAreas.remove(toDeleteIndex); // remove CodingArea
            tabbedPaneMethods.removeTabAt(toDeleteIndex); // remove tabbed pane
          }
        });
    btnDeleteMethod.setPreferredSize(new Dimension(100, 36));
    return btnDeleteMethod;
  }

  private JButton getBtnAddMethod(
      CodingFile codingFile, JTextField textFieldMethodName, CodingArea mainCodingArea) {
    JButton btnAddMethod = new JButton("Add");
    btnAddMethod.addActionListener(
        arg0 -> {
          String newMethodName = textFieldMethodName.getText();
          if (!newMethodName.isEmpty()) {
            if (!newMethodName.contains(" ")) {
              if ((!listModel.contains(newMethodName))
                  && (!codingFile.checkIfMethodWithSameNameExists(newMethodName))) {
                listModel.addElement(textFieldMethodName.getText());
                codingFile.methods.add(
                    new Methods(
                        newMethodName, "public static void " + newMethodName + "(){\n\t\n}"));
                int index = tabbedPaneMethods.getTabCount() - 1;
                CodingArea newCodingArea =
                    new CodingArea(
                        codingFile.returnMethodFromName(newMethodName),
                        terminal.getRunButton(),
                        terminal.getSaveButton(),
                        mainCodingArea.getTextArea().getFont());
                listOfCodingAreas.add(newCodingArea);
                tabbedPaneMethods.insertTab(newMethodName, null, newCodingArea, null, index);
                FileManager.save(codingFile);
              }
            }
          }
        });
    btnAddMethod.setPreferredSize(new Dimension(100, 36));
    return btnAddMethod;
  }
}
