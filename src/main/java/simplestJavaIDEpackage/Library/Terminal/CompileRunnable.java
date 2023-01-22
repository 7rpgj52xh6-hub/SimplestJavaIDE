package simplestJavaIDEpackage.Library.Terminal;

import java.io.IOException;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class CompileRunnable implements Runnable {

  private Command cmd;
  private JButton runButton;
  private String command;
  private CodingFile cf;

  public CompileRunnable(Command cmd, JButton runButton, CodingFile cf) {
    this.cmd = cmd;
    this.runButton = runButton;
    this.command = "javac " + cf.getAbsolutePath();
    this.cf = cf;
  }

  @Override
  public void run() {
    System.out.println("Compile ran");
    if (!cmd.isRunning()) {
      if (!cmd.run(command, runButton)) {
        runButton.setEnabled(false);
      } else {
        SwingUtilities.invokeLater(new RunRunnable(cmd,
            "java -cp " + cf.getClassPath() + " " + cf.getClassName(), runButton));
      }
    } else {
      try {
        cmd.send(command + "\n");
      } catch (IOException ex) {
        ErrorPopupWindow.throwMessage("Failed to run command:" + ex.getMessage());
      }
    }
  }
}
