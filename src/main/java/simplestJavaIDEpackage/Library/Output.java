package simplestJavaIDEpackage.Library;

import java.io.IOException;
import javax.swing.JButton;
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
public class Output extends JTextArea implements CommandListener {
  private static final long serialVersionUID = 4716862595957472820L;
  private Command cmd;

  public enum CommandType {
    COMPILE, RUN
  }

  public Output() {
    cmd = new Command(this);
  }

  public Command getCommand() {
    return this.cmd;
  }

  public void appendText(String text) {
    this.append(text);
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

  public void run(CommandType ct, CodingFile cf, JButton runButton, JButton compileButton) {
    if (ct == CommandType.COMPILE) {
      String command = "javac " + cf.getAbsolutePath();
      if (!cmd.isRunning()) {
        cmd.execute(command, runButton, compileButton);
      } else {
        try {
          cmd.send(command + "\n");
        } catch (IOException ex) {
          ErrorPopupWindow.main(null,
              "!! Failed to send compile command to process:" + ex.getMessage());
        }
      }
    } else if (ct == CommandType.RUN) {
      String command = "java -cp " + cf.getClassPath() + " " + cf.getClassName();
      if (!cmd.isRunning()) {
        cmd.execute(command, runButton, compileButton);
      } else {
        try {
          cmd.send(command + "\n");
        } catch (IOException ex) {
          ErrorPopupWindow.main(null,
              "!! Failed to send run command to process:" + ex.getMessage());
        }
      }
    } else {
      ErrorPopupWindow.main(null, "Commands other than compiling and running are not allowed");
    }
  }

}
