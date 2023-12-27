package simplestJavaIDEpackage.Library.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.TerminalPanel.CommandType;

public class Runner extends Thread {
  private List<String> cmds;
  private CommandListener listener;
  private Process process;
  private CommandType commandType;

  public Runner(CommandListener listener, List<String> cmds, CommandType commandType) {
    this.commandType = commandType;
    this.cmds = cmds;
    this.listener = listener;
    start();
  }

  public void setCommandType(CommandType commandType) {
    this.commandType = commandType;
  }

  @Override
public void run() {
    try {
      ProcessBuilder pb = new ProcessBuilder(cmds);
      pb.redirectErrorStream();
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
