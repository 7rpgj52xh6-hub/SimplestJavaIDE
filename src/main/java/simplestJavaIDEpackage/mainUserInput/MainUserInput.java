package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.CodingFile.CodeMode;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.ImprintWindow;
import simplestJavaIDEpackage.Library.CodingArea;
import simplestJavaIDEpackage.Library.CustomTextPane;
import simplestJavaIDEpackage.Library.Terminal;
import simplestJavaIDEpackage.Library.Terminal.CommandType;
import simplestJavaIDEpackage.mainUserInput.Components.HelpButton;
import simplestJavaIDEpackage.mainUserInput.Components.RunButton;
import simplestJavaIDEpackage.mainUserInput.Components.SaveButton;
import simplestJavaIDEpackage.mainUserInput.Components.ZoomInButton;
import simplestJavaIDEpackage.mainUserInput.Components.ZoomOutButton;;

/**
 * 
 * @author Daniel Trageser This class implements the main user interface and its functions
 *
 */
public class MainUserInput {

  private JFrame frmSimplestJavaIDE;
  private Terminal terminal;
  private static CustomTextPane informationTextPane;
  private CodeMode codeMode;
  private JTextField userInputTextField;

  /**
   * Launch the application.
   */
  public static void main(String[] args, CodingFile savefile) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          MainUserInput window = new MainUserInput(savefile);
          window.frmSimplestJavaIDE.setVisible(true);
        } catch (Exception e) {
          ErrorPopupWindow.throwMessage(e.getMessage());
        }
      }
    });
  }

  /**
   * Create the application.
   */
  public MainUserInput(CodingFile savefile) {
    codeMode = CodeMode.STANDARD;
    initialize(savefile);
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize(CodingFile codingFile) {
    // Main Window
    frmSimplestJavaIDE = new JFrame("Simplest Java IDE - " + codingFile.getAbsolutePath());
    frmSimplestJavaIDE.setSize(1080, 720);
    frmSimplestJavaIDE.setMinimumSize(new Dimension(1080, 720));
    frmSimplestJavaIDE.getContentPane().setLayout(new BorderLayout(0, 0));

    // Set Icon
    try {
      frmSimplestJavaIDE
          .setIconImage(ImageIO.read(getClass().getClassLoader().getResource("favicon.png")));
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }

    // Structure of main window
    JSplitPane contentSplitPane = new JSplitPane();
    contentSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    frmSimplestJavaIDE.getContentPane().add(contentSplitPane, BorderLayout.CENTER);
    contentSplitPane.setResizeWeight(0.9);
    contentSplitPane.setDividerLocation(frmSimplestJavaIDE.getHeight() - 300);
    contentSplitPane.getBottomComponent().setMinimumSize(new Dimension(20000, 286));

    JPanel bottomPanel = new JPanel();
    contentSplitPane.setBottomComponent(bottomPanel);
    bottomPanel.setPreferredSize(new Dimension(200, 286));
    bottomPanel.setLayout(new BorderLayout(0, 0));

    JPanel panelButtonsAndInformationTextPane = new JPanel();
    bottomPanel.add(panelButtonsAndInformationTextPane, BorderLayout.LINE_START);
    panelButtonsAndInformationTextPane.setMaximumSize(new Dimension(278, 286));
    panelButtonsAndInformationTextPane.setPreferredSize(new Dimension(278, 286));
    panelButtonsAndInformationTextPane.setLayout(new BorderLayout(0, 0));
    panelButtonsAndInformationTextPane.setBackground(new Color(47, 47, 47));

    // Information field on the bottom
    informationTextPane = new CustomTextPane();
    informationTextPane.setText(null);
    informationTextPane.setEditable(false);
    informationTextPane.setFocusable(false);
    informationTextPane.setBackground(new Color(40, 40, 40));
    DefaultCaret informationTextPaneCaret = (DefaultCaret) informationTextPane.getCaret();
    informationTextPaneCaret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    JScrollPane scrollPaneInformationTextPane = new JScrollPane(informationTextPane);
    scrollPaneInformationTextPane.setBounds(6, 173, 264, 70);
    panelButtonsAndInformationTextPane.add(scrollPaneInformationTextPane, BorderLayout.CENTER);

    JPanel panelButtons = new JPanel();
    panelButtonsAndInformationTextPane.add(panelButtons, BorderLayout.NORTH);
    panelButtons.setMaximumSize(new Dimension(278, 90));
    panelButtons.setPreferredSize(new Dimension(278, 90));
    panelButtons.setBackground(new Color(47, 47, 47));
    panelButtons.setLayout(null);

    JButton btnAddImports = new JButton("Add imports");
    btnAddImports.setBounds(10, 6, 126, 36);
    panelButtons.add(btnAddImports);

    // Action Panel
    JPanel actionPanel = new JPanel();
    actionPanel.setBounds(6, 48, 264, 36);
    actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
    actionPanel.setOpaque(false);
    panelButtons.add(actionPanel);

    // Action buttons
    SaveButton saveButton = new SaveButton();
    RunButton runButton = new RunButton();
    HelpButton helpButton = new HelpButton();
    ZoomInButton zoomInButton = new ZoomInButton();
    ZoomOutButton zoomOutButton = new ZoomOutButton();

    // Add buttons to action panel
    actionPanel.add(helpButton);
    actionPanel.add(zoomInButton);
    actionPanel.add(zoomOutButton);
    actionPanel.add(saveButton);
    actionPanel.add(runButton);

    // Coding input and load code if code is not null (from loading file)
    CodingArea codingArea =
        new CodingArea(codingFile, runButton.getButton(), saveButton.getButton());
    contentSplitPane.setTopComponent(codingArea);

    // Output
    terminal = new Terminal(userInputTextField, codingFile, runButton.getButton());
    bottomPanel.add(terminal);

    helpButton.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ImprintWindow.main(null);
      }
    });
    btnAddImports.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (codeMode == CodeMode.STANDARD || codeMode == CodeMode.ADVANCED
            || codeMode == CodeMode.EXPERT) {
          AddImportsWindow.main(codingFile, codingArea.getTextArea().getText(), codeMode,
              codingArea.getFont());
        } else {
          ErrorPopupWindow
              .throwMessage("Error with mode switch button. Mode was not set correcty.");
        }
        codingArea.save(codingFile); // Also save other code
      }
    });
    saveButton.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        codingArea.save(codingFile);
      }
    });
    runButton.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Save, compile and run
        codingArea.save(codingFile);
        informationTextPane.append("Compiling Code...!\n");
        if (terminal.tryRunning(CommandType.COMPILE)) {
          terminal.tryRunning(CommandType.RUN);
        }
      }
    });
    zoomInButton.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Add Zoom
        if (codeMode == CodeMode.STANDARD || codeMode == CodeMode.ADVANCED
            || codeMode == CodeMode.EXPERT) {
          Font font = codingArea.getFont();
          if (font.getSize() <= 45) {
            codingArea.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
            terminal.getTextArea()
                .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
          }
        } else {
          ErrorPopupWindow
              .throwMessage("Error with mode switch button. Mode was not set correcty.");
        }
      }
    });
    zoomOutButton.getButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Subtract Zoom
        if (codeMode == CodeMode.STANDARD || codeMode == CodeMode.ADVANCED
            || codeMode == CodeMode.EXPERT) {
          Font font = codingArea.getFont();
          if (font.getSize() >= 4) {
            codingArea.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
            terminal.getTextArea()
                .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
          }
        } else {
          ErrorPopupWindow
              .throwMessage("Error with mode switch button. Mode was not set correcty.");
        }
      }
    });
  }

  public static CustomTextPane getInformationTextPane() {
    return informationTextPane;
  }
}
