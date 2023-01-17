package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.CodingFile.CodeMode;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.ImprintWindow;
import simplestJavaIDEpackage.Library.AppendTask;
import simplestJavaIDEpackage.Library.Command;
import simplestJavaIDEpackage.Library.CommandListener;
import simplestJavaIDEpackage.Library.InformationTextPane;
import simplestJavaIDEpackage.Library.ProtectedDocumentFilter;
import simplestJavaIDEpackage.Library.Terminal;

/**
 * 
 * @author Daniel Trageser This class implements the main user interface and its functions
 *
 */
public class MainUserInput implements CommandListener, Terminal {

  private JFrame frmSimplestJavaIDE;
  private JTextArea terminal;
  private int userInputStart = 0;
  private Command cmd;
  private InformationTextPane informationTextPane;
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
          ErrorPopupWindow.main(null, e.getMessage());
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
      ErrorPopupWindow.main(null, e1.getMessage());
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
    informationTextPane = new InformationTextPane();
    informationTextPane.setText(null);
    informationTextPane.setEditable(false);
    informationTextPane.setFocusable(false);
    informationTextPane.setBackground(new Color(40, 40, 40));
    JScrollPane scrollPaneInformationTextPane = new JScrollPane(informationTextPane);
    scrollPaneInformationTextPane.setBounds(6, 215, 264, 70);
    panelButtonsAndInformationTextPane.add(scrollPaneInformationTextPane, BorderLayout.CENTER);

    JPanel panelButtons = new JPanel();
    panelButtonsAndInformationTextPane.add(panelButtons, BorderLayout.NORTH);
    panelButtons.setMaximumSize(new Dimension(278, 175));
    panelButtons.setPreferredSize(new Dimension(278, 175));
    panelButtons.setBackground(new Color(47, 47, 47));
    panelButtons.setLayout(null);

    // Buttons
    JButton btnSave = new JButton("Save");
    btnSave.setBounds(6, 90, 86, 36);
    btnSave.setEnabled(false);
    panelButtons.add(btnSave);

    JButton btnAddImports = new JButton("Add imports");
    btnAddImports.setBounds(6, 6, 131, 36);
    panelButtons.add(btnAddImports);

    JButton btnHelp = new JButton("Help");
    btnHelp.setIcon(null);
    btnHelp.setBounds(184, 48, 86, 36);
    panelButtons.add(btnHelp);

    JButton btnSwitchCodeMode = new JButton("Mode: Standard");
    btnSwitchCodeMode.setBounds(140, 6, 131, 36);
    panelButtons.add(btnSwitchCodeMode);

    JButton btnRun = new JButton("Run");
    btnRun.setBounds(184, 90, 86, 36);
    btnRun.setEnabled(false);
    panelButtons.add(btnRun);

    JButton btnCompile = new JButton("Compile");
    btnCompile.setBounds(95, 90, 86, 36);
    panelButtons.add(btnCompile);

    JButton btnZoomIn = new JButton("Zoom +");
    btnZoomIn.setBounds(6, 48, 86, 36);
    panelButtons.add(btnZoomIn);

    JButton btnZoomOut = new JButton("Zoom -");
    btnZoomOut.setBounds(95, 48, 86, 36);
    panelButtons.add(btnZoomOut);

    JLabel lblUserInput = new JLabel(
        "<html>\r\n\t<body>\r\n\t\t<h3 style=\"text-align: center\">User Input:</h3>\r\n\t</body>\r\n</html>");
    lblUserInput.setVerticalAlignment(SwingConstants.TOP);
    lblUserInput.setBounds(10, 131, 86, 36);
    panelButtons.add(lblUserInput);

    // Input
    userInputTextField = new JTextField();
    userInputTextField.setBounds(98, 133, 170, 36);
    panelButtons.add(userInputTextField);
    userInputTextField.setColumns(1);

    userInputTextField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!cmd.isRunning()) {
          cmd.execute(userInputTextField.getText(), btnRun, btnCompile);
          informationTextPane.append("User input was: " + userInputTextField.getText() + "\n");
          userInputTextField.setText(null);
        } else {
          try {
            cmd.send(userInputTextField.getText() + "\n");
            informationTextPane.append("User input was: " + userInputTextField.getText() + "\n");
            userInputTextField.setText(null);
          } catch (IOException ex) {
            informationTextPane.append("!! Failed to send command to process: " + ex.getMessage());
          }
        }
      }
    });

    // Coding input and load code if code is not null (from loading file)
    RSyntaxTextArea codingArea = new RSyntaxTextArea(20, 60);
    codingArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    codingArea.setCodeFoldingEnabled(true);
    try {
      Theme theme = Theme
          .load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
      theme.apply(codingArea);
    } catch (IOException e) {
      ErrorPopupWindow.main(null, e.getMessage());
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
        btnCompile.setEnabled(true);
        btnRun.setEnabled(false);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        codingFile.isSaved = false;
        btnSave.setEnabled(true);
        btnCompile.setEnabled(true);
        btnRun.setEnabled(false);
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

    // Copied for full class mode
    // Coding input and load code if code is not null (from loading file)
    RSyntaxTextArea codingAreaClassMode = new RSyntaxTextArea(20, 60);
    LanguageSupportFactory.get().register(codingAreaClassMode);
    codingAreaClassMode.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    codingAreaClassMode.setCodeFoldingEnabled(true);
    try {
      Theme theme = Theme
          .load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
      theme.apply(codingAreaClassMode);
    } catch (IOException e) {
      ErrorPopupWindow.main(null, e.getMessage());
    }
    codingAreaClassMode.setCurrentLineHighlightColor(new Color(55, 55, 55));
    codingAreaClassMode.setBackground(new Color(47, 47, 47));
    codingAreaClassMode.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void changedUpdate(DocumentEvent arg0) {
        // Copy to original text area
        codingArea.setText(null);
        codingArea.append(codingAreaClassMode.getText());
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        codingFile.isSaved = false;
        btnSave.setEnabled(true);
        btnCompile.setEnabled(true);
        btnRun.setEnabled(false);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        codingFile.isSaved = false;
        btnSave.setEnabled(true);
        btnCompile.setEnabled(true);
        btnRun.setEnabled(false);
      }
    });
    codingAreaClassMode.addKeyListener(new KeyListener() {
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
        // do noting
      }
    });

    // Coding Panel without code completion for standard and extended mode
    JPanel codingPanel = new JPanel(new BorderLayout(0, 0));
    RTextScrollPane codingScrollPane = new RTextScrollPane(codingArea);
    codingPanel.add(codingScrollPane, BorderLayout.CENTER);
    // Load Code if possible
    boolean loadingEnabled = true;
    while (loadingEnabled) {
      if (codingFile.isFinishedProcessing) {
        loadingEnabled = false;
        codingArea.setText(codingFile.getCode(codeMode));
      }
    }
    // Coding Panel with code completion for standard and extended mode
    JPanel codingClassModePanel = new JPanel(new BorderLayout(0, 0));
    RTextScrollPane codingClassModeScrollPane = new RTextScrollPane(codingAreaClassMode);
    codingClassModePanel.add(codingClassModeScrollPane, BorderLayout.CENTER);

    // Panel to house both coding panels
    JPanel codingAreaPanel = new JPanel(new CardLayout());
    codingAreaPanel.add(codingPanel, "1");
    codingAreaPanel.add(codingClassModePanel, "2");
    contentSplitPane.setTopComponent(codingAreaPanel);

    // Output
    cmd = new Command(this);
    terminal = new JTextArea(20, 30);
    terminal.setFocusable(false);
    terminal.setEditable(false);
    terminal.setBackground(new Color(35, 35, 35));
    ((AbstractDocument) terminal.getDocument())
        .setDocumentFilter(new ProtectedDocumentFilter(this));
    JScrollPane terminalScrollPane = new JScrollPane(terminal);
    bottomPanel.add(terminalScrollPane);

    // Manage interactions
    btnSwitchCodeMode.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CardLayout layout = (CardLayout) codingAreaPanel.getLayout();
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
            layout.show(codingAreaPanel, "2");
            codingAreaClassMode.setText(null);
            codingAreaClassMode.append(codingFile.getCode(codeMode));
            break;
          case EXPERT:
            // Change to standard mode
            codeMode = CodeMode.STANDARD;
            btnSwitchCodeMode.setText("Mode: Standard");
            layout.show(codingAreaPanel, "1");
            codingArea.setText(null);
            codingArea.append(codingFile.getCode(codeMode));
            break;
          default:
            informationTextPane.append("Error with mode switch button. Mode was not set correcty.");
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
        if (codeMode == CodeMode.STANDARD || codeMode == CodeMode.ADVANCED) {
          AddImportsWindow.main(codingFile, codingArea.getText(), codeMode, codingArea.getFont());
        } else if (codeMode == CodeMode.EXPERT) {
          AddImportsWindow.main(codingFile, codingAreaClassMode.getText(), codeMode,
              codingAreaClassMode.getFont());
        } else {
          informationTextPane.append("Error with mode switch button. Mode was not set correcty.");
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
    btnRun.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // RUN AND SAVE
        save(codingArea, codingFile);
        informationTextPane.append("Running Application...\n");
        runApplication(terminal, codingArea, codingFile, btnRun, btnCompile);
        btnSave.setEnabled(false);
      }
    });
    btnCompile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // COMPILE AND SAVE
        save(codingArea, codingFile);
        compile(terminal, codingArea, codingFile, btnRun, btnCompile);
        informationTextPane.append("Compiling Code...!\n");
        btnSave.setEnabled(false);
        btnRun.setEnabled(true);
        btnCompile.setEnabled(false);
      }
    });
    btnZoomIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Add Zoom
        if (codeMode == CodeMode.STANDARD || codeMode == CodeMode.ADVANCED) {
          Font font = codingArea.getFont();
          if (font.getSize() <= 45) {
            codingArea.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
            codingAreaClassMode
                .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
            terminal.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
          }
        } else if (codeMode == CodeMode.EXPERT) {
          Font font = codingAreaClassMode.getFont();
          if (font.getSize() <= 45) {
            codingArea.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
            codingAreaClassMode
                .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
            terminal.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
          }
        } else {
          informationTextPane.append("Error with mode switch button. Mode was not set correcty.");
        }
      }
    });
    btnZoomOut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Subtract Zoom
        if (codeMode == CodeMode.STANDARD || codeMode == CodeMode.ADVANCED) {
          Font font = codingArea.getFont();
          if (font.getSize() >= 4) {
            codingArea.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
            codingAreaClassMode
                .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
            terminal.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
          }
        } else if (codeMode == CodeMode.EXPERT) {
          Font font = codingAreaClassMode.getFont();
          if (font.getSize() >= 4) {
            codingArea.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
            codingAreaClassMode
                .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
            terminal.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
          }
        } else {
          informationTextPane.append("Error with mode switch button. Mode was not set correcty.");
        }
      }
    });
  }

  public InformationTextPane getInformationTextPane() {
    return this.informationTextPane;
  }

  @Override
  public void commandOutput(String text) {
    SwingUtilities.invokeLater(new AppendTask(this, text));
  }

  @Override
  public void commandFailed(Exception exp) {
    SwingUtilities.invokeLater(new AppendTask(this, "Command failed - " + exp.getMessage()));
  }

  @Override
  public void commandCompleted(String cmd, int result) {
    appendText("\n");
  }

  protected void updateUserInputPos() {
    int pos = terminal.getCaretPosition();
    terminal.setCaretPosition(terminal.getText().length());
    userInputStart = pos;

  }

  @Override
  public int getUserInputStart() {
    return userInputStart;
  }

  @Override
  public void appendText(String text) {
    terminal.append(text);
    updateUserInputPos();
  }

  public void save(RSyntaxTextArea codingArea, CodingFile codingFile) {
    codingFile.writeAllCodeToArray(codingArea.getText(), codeMode);
    codingFile.saveToFile();
  }

  public void runCommand(String command, JButton runButton, JButton compileButton)
      throws IOException, BadLocationException {
    if (!cmd.isRunning()) {
      cmd.execute(command, runButton, compileButton);
    } else {
      try {
        cmd.send(command + "\n");
      } catch (IOException ex) {
        this.getInformationTextPane()
            .append("!! Failed to send command to process:" + ex.getMessage());
      }
    }
  }

  public void compile(JTextArea outputTextPane, RSyntaxTextArea codingArea, CodingFile codingFile,
      JButton runButton, JButton compileButton) {
    try {
      runCommand("javac " + codingFile.getAbsolutePath(), runButton, compileButton);
    } catch (IOException | BadLocationException e) {
      this.getInformationTextPane().append(e.getMessage());
    }
  }

  public void runApplication(JTextArea outputTextPane, RSyntaxTextArea codingArea,
      CodingFile codingFile, JButton runButton, JButton compileButton) {
    try {
      runCommand("java -cp " + codingFile.getClassPath() + " " + codingFile.getClassName(),
          runButton, compileButton);
    } catch (IOException | BadLocationException e) {
      this.getInformationTextPane().append(e.getMessage());
    }
  }
}
