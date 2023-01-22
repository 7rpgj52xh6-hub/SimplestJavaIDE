package simplestJavaIDEpackage.Library.Terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class Command {
  private CommandListener listener;
  private ProcessRunner runner;

  public Command(CommandListener listener) {
    this.listener = listener;
  }

  public boolean isRunning() {
    return runner != null && runner.isAlive();
  }

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

  /**
   * 
   * @param cmd is the command to run as string
   * @param runButton is the button do disable if it did run with errors
   * @return returns true if it could be run without errors
   */
  public boolean run(String cmd, JButton runButton) {
    runner = new ProcessRunner(listener, getValues(cmd));
    try {
      runner.join();
    } catch (InterruptedException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
    return !runner.checkIfCompiledWithErrors();
  }

  public void send(String cmd) throws IOException {
    runner.write(cmd);
  }
}
