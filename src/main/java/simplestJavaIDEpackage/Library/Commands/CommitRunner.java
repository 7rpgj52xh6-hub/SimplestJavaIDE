package simplestJavaIDEpackage.Library.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class CommitRunner extends Thread {
  private List<String> cmds;
  private CommandListener listener;
  private Process process;
  private boolean ranWithErrors;

  public CommitRunner(CommandListener listener, List<String> cmds) {
    this.cmds = cmds;
    this.listener = listener;
    run();
  }

  public void run() {
    try {
      ProcessBuilder pb = new ProcessBuilder(cmds);
      process = pb.start();
      InputStream errs = process.getErrorStream();
      if (errs.read() != -1) {
        StreamReader errorReader = new StreamReader(listener, errs);
        errorReader.join();
        ranWithErrors = true;
      } else {
        ranWithErrors = false;
      }
      process.waitFor();
      listener.commandOutput("Compiling completed");
      listener.commandCompleted();
    } catch (Exception e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
      listener.commandFailed(e);
    }
  }

  public void write(String text) throws IOException {

  }

  public boolean ranWithErrors() {
    return this.ranWithErrors;
  }
}
