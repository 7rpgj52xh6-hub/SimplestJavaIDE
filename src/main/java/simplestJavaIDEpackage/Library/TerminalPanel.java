package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.Commands.CommandListener;
import simplestJavaIDEpackage.Library.Commands.Runner;

/**
 * This class implements the terminal with all possible functions
 *
 * @author Daniel Trageser
 */
public class TerminalPanel extends JPanel implements CommandListener {
  private static final long serialVersionUID = 4716862595957472820L;
  private final CodingFile codingFile;
  private final TerminalPanel terminal = this;
  private JTextArea terminalOutput;
  private Runner runner;
  private JScrollPane terminalTextAreaScrollPane;
  private JTextField userInputTextField;
  private SaveButton saveButton;
  private RunButton runButton;
  private HelpButton helpButton;
  private ZoomInButton zoomInButton;
  private ZoomOutButton zoomOutButton;
  private JButton btnAddImports;

  public TerminalPanel(CodingFile codingFile) {
    initializeUI();
    this.codingFile = codingFile;
  }

  public void initializeUI() {
    // Input
    userInputTextField = new JTextField();
    userInputTextField.setBorder(BorderFactory.createLineBorder(new Color(47, 47, 47), 6));
    userInputTextField.setColumns(1);
    userInputTextField.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            terminal.run(CommandType.INPUT);
            userInputTextField.setText(null);
          }
        });

    // Top Panel
    JPanel panelClearBtnAndLabel = new JPanel();
    panelClearBtnAndLabel.setLayout(null);
    panelClearBtnAndLabel.setPreferredSize(new Dimension(184, 48));
    panelClearBtnAndLabel.setBackground(new Color(47, 47, 47));
    JButton btnClearConsole = new JButton("Clear Console");
    btnClearConsole.setBounds(6, 6, 86, 36);
    btnClearConsole.addActionListener(
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
            terminal.getTextArea().setText(null);
          }
        });
    panelClearBtnAndLabel.add(btnClearConsole);

    JLabel lblUserInput =
        new JLabel(
            "<html>\r\n\t<body>\r\n\t\t<h3 style=\"text-align: center\">User Input:</h3>\r\n\t</body>\r\n</html>");
    lblUserInput.setBounds(104, 6, 86, 36);
    panelClearBtnAndLabel.add(lblUserInput);
    JPanel topPanel = new JPanel();
    topPanel.setPreferredSize(new Dimension(200, 48));
    topPanel.setLayout(new BorderLayout());

    // Action Panel
    JPanel actionPanel = new JPanel();
    actionPanel.setBounds(6, 48, 264, 36);
    actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
    actionPanel.setOpaque(false);

    // Button to add imports
    this.btnAddImports = new JButton("Add imports");
    btnAddImports.setPreferredSize(new Dimension(100, 36));
    btnAddImports.setBorder(BorderFactory.createLineBorder(new Color(47, 47, 47), 6));

    // Add elements to top panel
    JPanel panelActionAndClear = new JPanel();
    panelActionAndClear.setLayout(new BorderLayout());
    panelActionAndClear.setBackground(new Color(47, 47, 47));
    panelActionAndClear.add(actionPanel, BorderLayout.WEST);
    panelActionAndClear.add(btnAddImports, BorderLayout.CENTER);
    panelActionAndClear.add(panelClearBtnAndLabel, BorderLayout.EAST);
    topPanel.add(userInputTextField, BorderLayout.CENTER);
    topPanel.add(panelActionAndClear, BorderLayout.WEST);

    // Action buttons
    this.saveButton = new SaveButton();
    this.runButton = new RunButton();
    this.helpButton = new HelpButton();
    this.zoomInButton = new ZoomInButton();
    this.zoomOutButton = new ZoomOutButton();

    // Add buttons to action panel
    actionPanel.add(helpButton);
    actionPanel.add(zoomInButton);
    actionPanel.add(zoomOutButton);
    actionPanel.add(saveButton);
    actionPanel.add(runButton);

    // Output
    terminalOutput = new JTextArea();
    terminalTextAreaScrollPane = new JScrollPane();
    terminalTextAreaScrollPane.setViewportView(terminalOutput);
    DefaultCaret terminalTextAreaCaret = (DefaultCaret) terminalOutput.getCaret();
    terminalTextAreaCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    terminalTextAreaScrollPane.setFocusable(false);
    terminalOutput.setEditable(false);
    terminalOutput.setBackground(new Color(35, 35, 35));

    // Main Panel
    this.setLayout(new BorderLayout());
    this.add(terminalTextAreaScrollPane, BorderLayout.CENTER);
    this.add(topPanel, BorderLayout.NORTH);
  }

  public JButton getAddImportsButton() {
    return this.btnAddImports;
  }

  public JButton getSaveButton() {
    return this.saveButton.getButton();
  }

  public JButton getRunButton() {
    return this.runButton.getButton();
  }

  public JButton getHelpButton() {
    return this.helpButton.getButton();
  }

  public JButton getZoomInButton() {
    return this.zoomInButton.getButton();
  }

  public JButton getZoomOutButton() {
    return this.zoomOutButton.getButton();
  }

  public JTextArea getTextArea() {
    return this.terminalOutput;
  }

  @Override
  public void commandOutput(String text) {
    this.getTextArea().append(text);
  }

  @Override
  public void compileSuccessful() {
    // run java programm if compile was successful
    run(CommandType.RUN);
  }

  @Override
  public void compileFailed() {
    // TODO delete
    this.getTextArea().append("Compile failed");
  }

  @Override
  public void commandFailed(Exception exp) {
    this.getTextArea().append("Command failed - " + exp.getMessage());
  }

  public boolean isRunnerRunning() {
    return runner != null && runner.isAlive();
  }

  public void compile() {
    File javaTmpFile = new File(codingFile.getJavaTmpFilePath());
    codingFile.tmpSaveAndRunJavaCode();
    List<String> commandValues = Arrays.asList("javac", javaTmpFile.getAbsolutePath());
    runner = new Runner(this, commandValues, CommandType.COMPILE);
  }

  public void run(CommandType ct) {
    if (ct == CommandType.RUN) {
      List<String> commandValues =
          Arrays.asList(
              "java", "-cp", codingFile.generateClassPath(), codingFile._class.getClassName());
      runner = new Runner(this, commandValues, CommandType.RUN);
    } else if (ct == CommandType.INPUT) {
      try {
        // TODO Make possible to just press enter on input (empty input)
        String command = this.userInputTextField.getText();
        runner.write(command + "\n");
      } catch (IOException ex) {
        ErrorPopupWindow.throwMessage("!! Failed to send command to process:" + ex.getMessage());
      }
    }
  }

  public enum CommandType {
    COMPILE,
    RUN,
    INPUT
  }
}
