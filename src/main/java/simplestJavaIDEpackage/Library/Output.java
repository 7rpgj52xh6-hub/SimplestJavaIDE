package simplestJavaIDEpackage.Library;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
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
public class Output extends JScrollPane implements CommandListener {
  private static final long serialVersionUID = 4716862595957472820L;
  private JTextArea terminalTextArea;
  private JTextField userInputField;
  private ProcessRunner runner;

  public enum CommandType {
    COMPILE, RUN, INPUT
  }

  public Output(JTextField userInputField) {
    terminalTextArea = new JTextArea();
    DefaultCaret terminalTextAreaCaret = (DefaultCaret) terminalTextArea.getCaret();
    terminalTextAreaCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    this.userInputField = userInputField;
    this.setViewportView(terminalTextArea);
    this.setFocusable(false);
    this.getTextArea().setEditable(false);
    this.getTextArea().setBackground(new Color(35, 35, 35));
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
  public boolean tryRunning(CommandType ct, CodingFile cf, JButton runButton) {
    String command = "";
    switch (ct) {
      case COMPILE:
        command = "javac " + cf.getAbsolutePath();
        break;
      case RUN:
        command = "java -cp " + cf.getClassPath() + " " + cf.getClassName();
        break;
      case INPUT:
        if (userInputField.getText() != null) {
          command = userInputField.getText();
        }
        break;
      default:
        ErrorPopupWindow
            .throwMessage("Commands other than compiling and running java are not allowed");
        break;
    }
    if (!isRunning()) {
      runner = new ProcessRunner(this, getValues(command));
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
