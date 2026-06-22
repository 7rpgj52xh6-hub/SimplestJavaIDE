package simplestJavaIDEpackage.Library.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class StreamReader extends Thread {
  private final InputStream is;
  private final CommandListener listener;

  public StreamReader(CommandListener listener, InputStream is) {
    this.is = is;
    this.listener = listener;
    start();
  }

  @Override
  public void run() {
    try {
      byte[] buffer = new byte[1024];
      int count;
      while ((count = is.read(buffer)) != -1) {
        listener.commandOutput(new String(buffer, 0, count, StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
  }
}
