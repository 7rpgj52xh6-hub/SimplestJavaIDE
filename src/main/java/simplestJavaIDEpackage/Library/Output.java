package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
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
import simplestJavaIDEpackage.Library.Terminal.AppendTask;
import simplestJavaIDEpackage.Library.Terminal.CommandListener;
import simplestJavaIDEpackage.Library.Terminal.ProcessRunner;

/**
 * This class implements the terminal with all possible functions
 * 
 * @author Daniel Trageser
 * 
 */
public class Output extends JPanel implements CommandListener {
  private static final long serialVersionUID = 4716862595957472820L;
  private JTextArea terminalTextArea;
  private ProcessRunner runner;
  private CodingFile codingFile;
  private JScrollPane terminalTextAreaScrollPane;
  private JTextField userInputTextField;
  private JButton btnCompileAndRun;
  private Output terminal;

  public enum CommandType {
    COMPILE, RUN, INPUT
  }

  public Output(JTextField userInputField, CodingFile codingFile, JButton btnCompileAndRun) {
    initializeUI();
    this.codingFile = codingFile;
    this.btnCompileAndRun = btnCompileAndRun;
    this.terminal = this;
  }

  public void initializeUI() {
    // Input
    userInputTextField = new JTextField();
    userInputTextField.setBorder(BorderFactory.createLineBorder(new Color(47, 47, 47), 6));
    userInputTextField.setColumns(1);
    userInputTextField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        terminal.tryRunning(CommandType.INPUT);
        btnCompileAndRun.setEnabled(false);
        userInputTextField.setText(null);
      }
    });

    // Top Panel
    JPanel panelClearBtnAndLabel = new JPanel();
    panelClearBtnAndLabel.setLayout(null);
    panelClearBtnAndLabel.setPreferredSize(new Dimension(184, 48));
    panelClearBtnAndLabel.setBackground(new Color(47, 47, 47));
    JButton btnClearConsole = new JButton("Clear");
    btnClearConsole.setPreferredSize(new Dimension(86, 36));
    btnClearConsole.setBounds(6, 6, 86, 36);
    btnClearConsole.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        terminal.getTextArea().setText(null);
        // TODO enable following line
        // informationTextPane.setText(null);
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
    topPanel.add(userInputTextField, BorderLayout.CENTER);
    topPanel.add(panelClearBtnAndLabel, BorderLayout.LINE_START);

    // Output
    terminalTextArea = new JTextArea();
    terminalTextAreaScrollPane = new JScrollPane();
    terminalTextAreaScrollPane.setViewportView(terminalTextArea);
    DefaultCaret terminalTextAreaCaret = (DefaultCaret) terminalTextArea.getCaret();
    terminalTextAreaCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    terminalTextAreaScrollPane.setFocusable(false);
    terminalTextArea.setEditable(false);
    terminalTextArea.setBackground(new Color(35, 35, 35));

    // Main Panel
    this.setLayout(new BorderLayout());
    this.add(terminalTextArea, BorderLayout.CENTER);
    this.add(topPanel, BorderLayout.NORTH);
  }

  public JTextArea getTextArea() {
    return this.terminalTextArea;
  }

  public void appendText(String text) {
    this.getTextArea().append(text);
  }

  @Override
  public void commandOutput(String text) {
    SwingUtilities.invokeLater(new AppendTask(this, text));
  }

  @Override
  public void commandCompleted(String cmd, int result) {
    appendText("\n");
  }


  @Override
  public void commandFailed(Exception exp) {
    ErrorPopupWindow.throwMessage("Command failed - " + exp.getMessage());
  }

  public boolean isRunning() {
    return runner != null && runner.isAlive();
  }

  /**
   * Sends compile or run command to system terminal. Returns true if errors occurred
   * 
   * @param ct defines if compile or run
   * @param cf is used for the path that is used for the commands
   */
  public boolean tryRunning(CommandType ct) {
    String command = "";
    switch (ct) {
      case COMPILE:
        command = "javac " + codingFile.getAbsolutePath();
        break;
      case RUN:
        command = "java -cp " + codingFile.getClassPath() + " " + codingFile.getClassName();
        break;
      case INPUT:
        if (userInputTextField.getText() != null) {
          command = userInputTextField.getText();
        }
        break;
      default:
        ErrorPopupWindow
            .throwMessage("Commands other than compiling and running java are not allowed");
        break;
    }
    if (!isRunning()) {
      runner = new ProcessRunner(this, getValues(command));
      if (ct == CommandType.COMPILE) {
        try {
          runner.join();
          if (runner.ranWithErrors()) {
            return false;
          } else {
            return true;
          }
        } catch (InterruptedException e) {
          ErrorPopupWindow.throwMessage(e.getMessage());
        }
      } else {
        return true;
      }
    } else {
      try {
        runner.write(command + "\n");
      } catch (IOException ex) {
        ErrorPopupWindow
            .throwMessage("!! Failed to send compile command to process:" + ex.getMessage());
      }
    }
    return false;
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
