package simplestJavaIDEpackage.Library.Terminal;

import java.io.IOException;
import javax.swing.JButton;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class RunRunnable implements Runnable {

  private Command cmd;
  private JButton runButton;
  private String command;

  public RunRunnable(Command cmd, String command, JButton runButton) {
    this.cmd = cmd;
    this.runButton = runButton;
    this.command = command;
  }

  @Override
  public void run() {
    System.out.println("run ran");
    if (!cmd.isRunning()) {
      cmd.run(command, runButton);
    } else {
      try {
        cmd.send(command + "\n");
      } catch (IOException ex) {
        ErrorPopupWindow.throwMessage("Failed to run command:" + ex.getMessage());
      }
    }
  }
}
