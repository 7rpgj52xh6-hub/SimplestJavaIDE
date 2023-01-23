package simplestJavaIDEpackage.Library;

import java.awt.Color;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.Terminal.AppendTask;
import simplestJavaIDEpackage.Library.Terminal.Command;
import simplestJavaIDEpackage.Library.Terminal.CommandListener;

/**
 * This class implements the terminal with all possible functions
 * 
 * @author Daniel Trageser
 * 
 */
public class Output extends JScrollPane implements CommandListener {
  private static final long serialVersionUID = 4716862595957472820L;
  private Command cmd;
  private JTextArea terminalTextArea;
  private JTextField userInputField;

  public enum CommandType {
    COMPILE, RUN, INPUT
  }

  public Output(JTextField userInputField) {
    cmd = new Command(this);
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

  // TODO Make obsolete
  public Command getCommand() {
    return this.cmd;
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
    if (!cmd.isRunning()) {
      return cmd.run(command);
    } else {
      try {
        cmd.send(command + "\n");
      } catch (IOException ex) {
        ErrorPopupWindow
            .throwMessage("!! Failed to send compile command to process:" + ex.getMessage());
      }
    }
    return false;
  }


}
