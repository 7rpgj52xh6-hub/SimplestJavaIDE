package simplestJavaIDEpackage.Library.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.TerminalPanel.CommandType;

public class Runner extends Thread {
  private final List<String> commands;
  private final CommandListener listener;
  private final CommandType commandType;
  private Process process;

  public Runner(CommandListener listener, List<String> commands, CommandType commandType) {
    this.commandType = commandType;
    this.commands = commands;
    this.listener = listener;
    start();
  }

  @Override
  public void run() {
    try {
      ProcessBuilder pb = new ProcessBuilder(commands);
      process = pb.start();
      InputStream inputStream = process.getInputStream();
      InputStream errorStream = process.getErrorStream();
      StreamReader inputReader = new StreamReader(listener, inputStream);
      if (errorStream.read() != -1) {
        StreamReader errorReader = new StreamReader(listener, errorStream);
        errorReader.join();
        if (commandType == CommandType.COMPILE) {
          listener.compileFailed();
        }
      } else {
        if (commandType == CommandType.COMPILE) {
          listener.compileSuccessful();
        }
      }
      process.waitFor();
      inputReader.join();
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
}
