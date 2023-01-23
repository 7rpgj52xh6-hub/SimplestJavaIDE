package simplestJavaIDEpackage.Library.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class CommandRunner extends Thread {
  private List<String> cmds;
  private CommandListener listener;
  private Process process;
  private boolean ranWithErrors;

  public CommandRunner(CommandListener listener, List<String> cmds) {
    this.cmds = cmds;
    this.listener = listener;
    start();
  }

  public void run() {
    try {
      ProcessBuilder pb = new ProcessBuilder(cmds);
      process = pb.start();
      InputStream is = process.getInputStream();
      InputStream errs = process.getErrorStream();
      StreamReader reader = new StreamReader(listener, is);
      if (errs.read() != -1) {
        StreamReader errorReader = new StreamReader(listener, errs);
        errorReader.join();
        ranWithErrors = true;
      } else {
        ranWithErrors = false;
      }
      int result = process.waitFor();
      reader.join();
      listener.commandCompleted();
    } catch (Exception e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
      listener.commandFailed(e);
    }
  }

  public void write(String text) throws IOException {
    if (process != null && process.isAlive()) {
      process.getOutputStream().write(text.getBytes());
      process.getOutputStream().flush();
    }
  }

  public boolean ranWithErrors() {
    return this.ranWithErrors;
  }
}
