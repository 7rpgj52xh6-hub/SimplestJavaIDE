package simplestJavaIDEpackage.Library.Commands;

import java.io.IOException;
import java.util.List;
import simplestJavaIDEpackage.ErrorPopupWindow;

/**
 * Runs the user's compiled program in a separate process and streams its output
 * (stdout and stderr) back to the terminal. Kept separate from compilation so
 * the program gets its own stdin/stdout and an endless loop can't freeze the IDE
 * (and can be stopped via {@link #kill()}).
 *
 * @author Daniel Trageser
 */
public class Runner extends Thread {
  private final List<String> commands;
  private final CommandListener listener;
  private Process process;

  public Runner(CommandListener listener, List<String> commands) {
    this.commands = commands;
    this.listener = listener;
    start();
  }

  @Override
  public void run() {
    try {
      ProcessBuilder pb = new ProcessBuilder(commands);
      process = pb.start();
      StreamReader outputReader = new StreamReader(listener, process.getInputStream(), false);
      StreamReader errorReader = new StreamReader(listener, process.getErrorStream(), true);
      process.waitFor();
      outputReader.join();
      errorReader.join();
    } catch (Exception e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
      listener.commandFailed(e);
    } finally {
      listener.commandFinished();
    }
  }

  public void write(String text) throws IOException {
    if (process != null && process.isAlive()) {
      process.getOutputStream().write(text.getBytes());
      process.getOutputStream().flush();
    }
  }

  public boolean isRunning() {
    return process != null && process.isAlive();
  }

  /** Forcibly stops the running program (and its descendants). */
  public void kill() {
    if (process == null) {
      return;
    }
    process.descendants().forEach(ProcessHandle::destroy);
    process.destroy();
    if (process.isAlive()) {
      process.destroyForcibly();
    }
  }
}
