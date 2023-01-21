package simplestJavaIDEpackage.Library;

import java.awt.Color;
import java.io.IOException;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
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

  public enum ErrorsHappened {
    YES, NO, UNDEFINED
  }

  public enum CommandType {
    COMPILE, RUN
  }

  public Output() {
    cmd = new Command(this);
    terminalTextArea = new JTextArea();
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
    SwingUtilities.invokeLater(new AppendTask(this, "Command failed - " + exp.getMessage()));
  }

  /**
   * Sends compile or run command to system terminal. Returns true if errors occured
   * 
   * @param ct defines if compile or run
   * @param cf is used for the path that is used for the commands
   */
  public ErrorsHappened run(CommandType ct, CodingFile cf) {

    if (ct == CommandType.COMPILE) {
      String command = "javac " + cf.getAbsolutePath();
      if (!cmd.isRunning()) {
        return cmd.execute(command);
      } else {
        try {
          cmd.send(command + "\n");
        } catch (IOException ex) {
          ErrorPopupWindow
              .throwMessage("!! Failed to send compile command to process:" + ex.getMessage());
        }
      }
    } else if (ct == CommandType.RUN) {
      String command = "java -cp " + cf.getClassPath() + " " + cf.getClassName();
      if (!cmd.isRunning()) {
        return cmd.execute(command);
      } else {
        try {
          cmd.send(command + "\n");
        } catch (IOException ex) {
          ErrorPopupWindow
              .throwMessage("!! Failed to send run command to process:" + ex.getMessage());
        }
      }
    } else {
      ErrorPopupWindow
          .throwMessage("Commands other than compiling and running java are not allowed");
    }
    return ErrorsHappened.UNDEFINED;
  }

}
