package simplestJavaIDEpackage;

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
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import simplestJavaIDEpackage.Library.AddImportsWindow;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.FileManager;
import simplestJavaIDEpackage.Library.CodeStructure.Methods;
import simplestJavaIDEpackage.Library.CodingArea;
import simplestJavaIDEpackage.Library.MethodManagerPanel;
import simplestJavaIDEpackage.Library.MethodTabsPanel;
import simplestJavaIDEpackage.Library.TerminalPanel;

/**
 * @author Daniel Trageser This class implements the main user interface and its functions
 */
public class MainUserInput {


  private JFrame frmSimplestJavaIDE;
  private TerminalPanel terminal;

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
            window.frmSimplestJavaIDE.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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

    MethodTabsPanel methodTabsPanel = new MethodTabsPanel(codingFile, terminal);
    MainUserInputSplitPane.setLeftComponent(methodTabsPanel);
    

    // Action button interactions
    terminal.getHelpButton().addActionListener(e -> ImprintWindow.main(null));
    terminal
        .getSaveButton()
        .addActionListener(
            e -> {
              for (CodingArea i : methodTabsPanel.getListOfCodingAreas()) {
                codingFile.methods.set(
                		methodTabsPanel.getListOfCodingAreas().indexOf(i),
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
              for (CodingArea i : methodTabsPanel.getListOfCodingAreas()) {
                codingFile.methods.set(
                		methodTabsPanel.getListOfCodingAreas().indexOf(i),
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
                Font font = methodTabsPanel.getMainCodingArea().getTextArea().getFont();
                if (font.getSize() <= 60) {
                  for (CodingArea i : methodTabsPanel.getListOfCodingAreas()) {
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
                Font font = methodTabsPanel.getMainCodingArea().getTextArea().getFont();
                if (font.getSize() >= 10) {
                  for (CodingArea i : methodTabsPanel.getListOfCodingAreas()) {
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
}
