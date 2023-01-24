package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.Commands.AppendTask;
import simplestJavaIDEpackage.Library.Commands.CommandListener;
import simplestJavaIDEpackage.Library.Commands.Runner;

/**
 * This class implements the terminal with all possible functions
 * 
 * @author Daniel Trageser
 * 
 */
public class TerminalPanel extends JPanel implements CommandListener {
  private static final long serialVersionUID = 4716862595957472820L;
  private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
  private JTextArea terminalOutput;
  private Runner runner;
  private CodingFile codingFile;
  private JScrollPane terminalTextAreaScrollPane;
  private JTextField userInputTextField;
  private TerminalPanel terminal = this;
  private List<String> outputList;
  private SaveButton saveButton;
  private RunButton runButton;
  private HelpButton helpButton;
  private ZoomInButton zoomInButton;
  private ZoomOutButton zoomOutButton;
  private JButton btnAddImports;

  public enum CommandType {
    COMPILE, RUN, INPUT
  }

  public TerminalPanel(JTextField userInputField, CodingFile codingFile) {
    initializeUI();
    this.codingFile = codingFile;
    outputList = new ArrayList<String>();
  }

  public void initializeUI() {
    // Input
    userInputTextField = new JTextField();
    userInputTextField.setBorder(BorderFactory.createLineBorder(new Color(47, 47, 47), 6));
    userInputTextField.setColumns(1);
    userInputTextField.addActionListener(new ActionListener() {
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
    btnClearConsole.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        terminal.getTextArea().setText(null);
      }
    });
    panelClearBtnAndLabel.add(btnClearConsole);

    JLabel lblUserInput = new JLabel(
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

  public void appendText(String text) {
    this.getTextArea().append(text);
  }

  @Override
  public void commandOutput(String text) {
    SwingUtilities.invokeLater(new AppendTask(terminal, text));
    // outputList.add(text);
  }

  @Override
  public void commandCompleted() {
    // Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    // String prefix = "[" + sdf.format(timestamp) + "] - ";
    // String output = "";
    // System.out.println(outputList.size());
    // for (String i : outputList) {
    // output = output + i;
    // }
    // appendText(prefix + output + "\n");
    // outputList.clear();
    appendText("\n");
  }

  @Override
  public void compileSuccessful() {
    // TODO delete
    appendText("Compile successful\n");
    run(CommandType.RUN);
  }

  @Override
  public void compileFailed() {
    // TODO delete
    appendText("Compile failed");
  }

  @Override
  public void commandFailed(Exception exp) {
    ErrorPopupWindow.throwMessage("Command failed - " + exp.getMessage());
  }

  public boolean isRunnerRunning() {
    return runner != null && runner.isAlive();
  }

  public void compile() {
    String command = "";
    runner =
        new Runner(this, getValues("javac " + codingFile.getAbsolutePath()), CommandType.COMPILE);
  }

  public void run(CommandType ct) {
    if (ct == CommandType.RUN) {
      String command = "java -cp " + codingFile.getClassPath() + " " + codingFile.getClassName();
      runner = new Runner(this, getValues(command), CommandType.RUN);
    } else if (ct == CommandType.INPUT) {
      try {
        String command = this.userInputTextField.getText();
        runner.write(command + "\n");
      } catch (IOException ex) {
        ErrorPopupWindow.throwMessage("!! Failed to send command to process:" + ex.getMessage());
      }
    }

  }


  // TODO Make obsolete
  public List<String> getValues(String cmd) {
    if (!cmd.trim().isEmpty()) {
      List<String> values = new ArrayList<>(25);
      if (cmd.contains("\"")) {

        while (cmd.contains("\"")) {

          String start = cmd.substring(0, cmd.indexOf("\""));
          cmd = cmd.substring(start.length());
          String quote = cmd.substring(cmd.indexOf("\"") + 1);
          cmd = cmd.substring(cmd.indexOf("\"") + 1);
          quote = quote.substring(0, cmd.indexOf("\""));
          cmd = cmd.substring(cmd.indexOf("\"") + 1);

          if (!start.trim().isEmpty()) {
            String parts[] = start.trim().split(" ");
            values.addAll(Arrays.asList(parts));
          }
          values.add(quote.trim());

        }

        if (!cmd.trim().isEmpty()) {
          String parts[] = cmd.trim().split(" ");
          values.addAll(Arrays.asList(parts));
        }
      } else {

        if (!cmd.trim().isEmpty()) {
          String parts[] = cmd.trim().split(" ");
          values.addAll(Arrays.asList(parts));
        }

      }
      return values;
    }
    return null;
  }
}
