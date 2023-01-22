package simplestJavaIDEpackage.Library.Terminal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class StreamReader extends Thread {
  private InputStream is;
  private CommandListener listener;
  private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  public StreamReader(CommandListener listener, InputStream is) {
    this.is = is;
    this.listener = listener;
    start();
  }

  @Override
  public void run() {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String start = ("[" + sdf.format(timestamp) + "]:\n");
    try {
      listener.commandOutput(start);
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
