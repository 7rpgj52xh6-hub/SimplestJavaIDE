package simplestJavaIDEpackage.Library.Commands;

import java.io.IOException;
import java.io.InputStream;

import simplestJavaIDEpackage.ErrorPopupWindow;

public class StreamReader extends Thread {
  private InputStream is;
  private CommandListener listener;

  public StreamReader(CommandListener listener, InputStream is) {
    this.is = is;
    this.listener = listener;
    start();
  }

  @Override
  public void run() {
    try {
      int value = -1;
      while ((value = is.read()) != -1) {
        listener.commandOutput(Character.toString((char) value));
      }
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
  }

  public int checkIfStreamIsEmpty() {
    try {
      return is.read();
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
    return 69;
  }
}
