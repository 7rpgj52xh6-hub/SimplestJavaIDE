package simplestJavaIDEpackage.Library.Terminal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.StringJoiner;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class ProcessRunner extends Thread {
  private List<String> cmds;
  private CommandListener listener;
  private Process process;
  private boolean isCompile;
  private boolean compiledWithErrors;


  public ProcessRunner(CommandListener listener, List<String> cmds) {
    this.cmds = cmds;
    this.listener = listener;
    start();
  }

  public void run() {
    try {
      ProcessBuilder pb = new ProcessBuilder(cmds);
      pb.redirectErrorStream();
      process = pb.start();
      InputStream errs = process.getErrorStream();
      int result;
      if (!isCompile) {
        InputStream is = process.getInputStream();
        StreamReader reader = new StreamReader(listener, is);
        if (errs.read() != -1) {
          StreamReader errorReader = new StreamReader(listener, errs);
          errorReader.join();
          this.compiledWithErrors = true;
          // TODO Prevent run if compile fails
        }
        result = process.waitFor();
        reader.join();
      } else {
        if (errs.read() != -1) {
          StreamReader errorReader = new StreamReader(listener, errs);
          errorReader.join();
          this.compiledWithErrors = true;
        }
        result = process.waitFor();
      }
      StringJoiner sj = new StringJoiner(" ");
      cmds.stream().forEach((cmd) -> {
        sj.add(cmd);
      });
      listener.commandCompleted(sj.toString(), result);
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

  public boolean checkIfCompiledWithErrors() {
    return this.compiledWithErrors;
  }
}
