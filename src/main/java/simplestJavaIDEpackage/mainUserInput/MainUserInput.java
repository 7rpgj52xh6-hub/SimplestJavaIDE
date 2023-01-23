package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.CodingFile.CodeMode;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.ImprintWindow;
import simplestJavaIDEpackage.Library.InfoTextPane;
import simplestJavaIDEpackage.Library.Output;
import simplestJavaIDEpackage.Library.Output.CommandType;;

/**
 * 
 * @author Daniel Trageser This class implements the main user interface and its functions
 *
 */
public class MainUserInput {

  private JFrame frmSimplestJavaIDE;
  private Output terminal;
  private static InfoTextPane informationTextPane;
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
    informationTextPane = new InfoTextPane();
    informationTextPane.setText(null);
    informationTextPane.setEditable(false);
    informationTextPane.setFocusable(false);
    informationTextPane.setBackground(new Color(40, 40, 40));
    DefaultCaret informationTextPaneCaret = (DefaultCaret) informationTextPane.getCaret();
    informationTextPaneCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
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

    // Help button
    JPanel panelBtnHelp = new JPanel(new BorderLayout());
    panelBtnHelp.setPreferredSize(new Dimension(44, 36));
    panelBtnHelp.setOpaque(false);
    JButton btnHelp = new JButton("Help");
    try {
      Image tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnHelp.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnHelp = new ImageIcon(tmpImage);
      tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnHelpPressed.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnHelpPressed = new ImageIcon(tmpImage);
      btnHelp.setIcon(iconBtnHelp);
      btnHelp.setPressedIcon(iconBtnHelpPressed);
      btnHelp.setText(null);
      btnHelp.setBorderPainted(false);
      btnHelp.setBorder(null);
      btnHelp.setMargin(new Insets(0, 0, 0, 0));
      btnHelp.setContentAreaFilled(false);
      btnHelp.setBounds(95, 90, 30, 30);
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }
    panelBtnHelp.add(btnHelp, BorderLayout.CENTER);


    JButton btnSwitchCodeMode = new JButton("Mode: Standard");
    btnSwitchCodeMode.setBounds(146, 6, 126, 36);
    panelButtons.add(btnSwitchCodeMode);

    // Save button
    JPanel panelBtnSave = new JPanel(new BorderLayout());
    panelBtnSave.setPreferredSize(new Dimension(66, 36));
    panelBtnSave.setOpaque(false);
    JButton btnSave = new JButton("Save");
    try {
      Image tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnSave.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnSave = new ImageIcon(tmpImage);
      tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnSavePressed.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnSavePressed = new ImageIcon(tmpImage);
      tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnSaveDisabled.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnSaveDisabled = new ImageIcon(tmpImage);
      btnSave.setIcon(iconBtnSave);
      btnSave.setPressedIcon(iconBtnSavePressed);
      btnSave.setDisabledIcon(iconBtnSaveDisabled);
      btnSave.setText(null);
      btnSave.setBorderPainted(false);
      btnSave.setBorder(null);
      btnSave.setMargin(new Insets(0, 0, 0, 0));
      btnSave.setContentAreaFilled(false);
      btnSave.setBounds(95, 90, 30, 30);
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }
    panelBtnSave.add(btnSave, BorderLayout.CENTER);


    // Run button
    JPanel panelBtnRun = new JPanel(new BorderLayout());
    panelBtnRun.setPreferredSize(new Dimension(66, 36));
    panelBtnRun.setOpaque(false);
    JButton btnCompileAndRun = new JButton("Run code");
    try {
      Image tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnRun.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnCompileAndRun = new ImageIcon(tmpImage);
      tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnRunPressed.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnCompileAndRunPressed = new ImageIcon(tmpImage);
      tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnRunDisabled.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnCompileAndRunDisabled = new ImageIcon(tmpImage);
      btnCompileAndRun.setIcon(iconBtnCompileAndRun);
      btnCompileAndRun.setPressedIcon(iconBtnCompileAndRunPressed);
      btnCompileAndRun.setDisabledIcon(iconBtnCompileAndRunDisabled);
      btnCompileAndRun.setText(null);
      btnCompileAndRun.setBorderPainted(false);
      btnCompileAndRun.setBorder(null);
      btnCompileAndRun.setMargin(new Insets(0, 0, 0, 0));
      btnCompileAndRun.setContentAreaFilled(false);
      btnCompileAndRun.setBounds(95, 90, 30, 30);
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }
    panelBtnRun.add(btnCompileAndRun, BorderLayout.CENTER);


    // ZoomIn Button
    JPanel panelBtnZoomIn = new JPanel(new BorderLayout());
    panelBtnZoomIn.setPreferredSize(new Dimension(44, 36));
    panelBtnZoomIn.setOpaque(false);
    JButton btnZoomIn = new JButton("+");
    btnZoomIn.setBounds(6, 48, 42, 36);
    try {
      Image tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnZoomIn.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnZoomIn = new ImageIcon(tmpImage);
      tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnZoomInPressed.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnZoomInPressed = new ImageIcon(tmpImage);
      btnZoomIn.setIcon(iconBtnZoomIn);
      btnZoomIn.setPressedIcon(iconBtnZoomInPressed);
      btnZoomIn.setText(null);
      btnZoomIn.setBorderPainted(false);
      btnZoomIn.setBorder(null);
      btnZoomIn.setMargin(new Insets(0, 0, 0, 0));
      btnZoomIn.setContentAreaFilled(false);
      btnZoomIn.setBounds(95, 90, 30, 30);
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }
    panelBtnZoomIn.add(btnZoomIn, BorderLayout.CENTER);


    // ZoomOut Button
    JPanel panelBtnZoomOut = new JPanel(new BorderLayout());
    panelBtnZoomOut.setPreferredSize(new Dimension(44, 36));
    panelBtnZoomOut.setOpaque(false);
    JButton btnZoomOut = new JButton("-");
    btnZoomOut.setBounds(50, 48, 42, 36);
    try {
      Image tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnZoomOut.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnZoomOut = new ImageIcon(tmpImage);
      tmpImage = ImageIO.read(getClass().getClassLoader().getResource("btnZoomOutPressed.png"))
          .getScaledInstance(30, 30, Image.SCALE_SMOOTH);
      ImageIcon iconBtnZoomOutPressed = new ImageIcon(tmpImage);
      btnZoomOut.setIcon(iconBtnZoomOut);
      btnZoomOut.setPressedIcon(iconBtnZoomOutPressed);
      btnZoomOut.setText(null);
      btnZoomOut.setBorderPainted(false);
      btnZoomOut.setBorder(null);
      btnZoomOut.setMargin(new Insets(0, 0, 0, 0));
      btnZoomOut.setContentAreaFilled(false);
      btnZoomOut.setBounds(95, 90, 30, 30);
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }
    panelBtnZoomOut.add(btnZoomOut, BorderLayout.CENTER);


    // Action panel layout
    actionPanel.add(panelBtnHelp);
    actionPanel.add(panelBtnZoomIn);
    actionPanel.add(panelBtnZoomOut);
    actionPanel.add(panelBtnSave);
    actionPanel.add(panelBtnRun);

    // Coding input and load code if code is not null (from loading file)
    RSyntaxTextArea codingArea = new RSyntaxTextArea(20, 60);
    codingArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    codingArea.setCodeFoldingEnabled(true);
    try {
      Theme theme = Theme
          .load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
      theme.apply(codingArea);
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
    codingArea.setCurrentLineHighlightColor(new Color(55, 55, 55));
    codingArea.setBackground(new Color(47, 47, 47));
    codingArea.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void changedUpdate(DocumentEvent arg0) {
        // Do nothing
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        codingFile.isSaved = false;
        btnSave.setEnabled(true);
        btnCompileAndRun.setEnabled(true);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        codingFile.isSaved = false;
        btnSave.setEnabled(true);
        btnCompileAndRun.setEnabled(true);
      }
    });
    codingArea.addKeyListener(new KeyListener() {
      @Override
      public void keyPressed(KeyEvent e) {
        boolean windowsCTRLpressed = ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0);
        boolean macOSCTRLpressed = ((e.getModifiersEx() & KeyEvent.VK_META) != 0);
        if ((e.getKeyCode() == KeyEvent.VK_S) && (windowsCTRLpressed || macOSCTRLpressed)) {
          save(codingArea, codingFile);
          btnSave.setEnabled(false);
        }
      }

      @Override
      public void keyReleased(KeyEvent arg0) {
        // do nothing
      }

      @Override
      public void keyTyped(KeyEvent arg0) {
        // do nothing
      }
    });

    // Coding Panel without code completion for standard and extended mode
    JPanel codingPanel = new JPanel(new BorderLayout(0, 0));
    RTextScrollPane codingScrollPane = new RTextScrollPane(codingArea);
    codingPanel.add(codingScrollPane, BorderLayout.CENTER);
    contentSplitPane.setTopComponent(codingPanel);
    // Load Code if possible
    boolean loadingEnabled = true;
    while (loadingEnabled) {
      if (codingFile.isFinishedProcessing) {
        loadingEnabled = false;
        codingArea.setText(codingFile.getCode(codeMode));
      }
    }

    // Output
    terminal = new Output(userInputTextField, codingFile, btnCompileAndRun);
    bottomPanel.add(terminal);

    // Manage interactions
    btnSwitchCodeMode.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save(codingArea, codingFile);
        switch (codeMode) {
          case STANDARD:
            // Change to extended mode
            codeMode = CodeMode.ADVANCED;
            btnSwitchCodeMode.setText("Mode: Advanced");
            codingArea.setText(null);
            codingArea.append(codingFile.getCode(codeMode));
            break;
          case ADVANCED:
            // Change to full mode
            codeMode = CodeMode.EXPERT;
            btnSwitchCodeMode.setText("Mode: Expert");
            codingArea.setText(null);
            codingArea.append(codingFile.getCode(codeMode));
            break;
          case EXPERT:
            // Change to standard mode
            codeMode = CodeMode.STANDARD;
            btnSwitchCodeMode.setText("Mode: Standard");
            codingArea.setText(null);
            codingArea.append(codingFile.getCode(codeMode));
            break;
          default:
            ErrorPopupWindow
                .throwMessage("Error with mode switch button. Mode was not set correcty.");
            break;
        }
      }
    });
    btnHelp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ImprintWindow.main(null);
      }
    });
    btnAddImports.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (codeMode == CodeMode.STANDARD || codeMode == CodeMode.ADVANCED
            || codeMode == CodeMode.EXPERT) {
          AddImportsWindow.main(codingFile, codingArea.getText(), codeMode, codingArea.getFont());
        } else {
          ErrorPopupWindow
              .throwMessage("Error with mode switch button. Mode was not set correcty.");
        }
        save(codingArea, codingFile); // Also save other code
        btnSave.setEnabled(false);
      }
    });
    btnSave.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save(codingArea, codingFile);
        btnSave.setEnabled(false);
      }
    });
    btnCompileAndRun.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Save, compile and run
        save(codingArea, codingFile);
        btnSave.setEnabled(false);
        informationTextPane.append("Compiling Code...!\n");
        if (terminal.tryRunning(CommandType.COMPILE)) {
          terminal.tryRunning(CommandType.RUN);
        }
      }
    });
    btnZoomIn.addActionListener(new ActionListener() {
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
    btnZoomOut.addActionListener(new ActionListener() {
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

  public static InfoTextPane getInformationTextPane() {
    return informationTextPane;
  }

  public void save(RSyntaxTextArea codingArea, CodingFile codingFile) {
    codingFile.writeAllCodeToArray(codingArea.getText(), codeMode);
    codingFile.saveToFile();
  }
}
