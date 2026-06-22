package simplestJavaIDEpackage;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Central place for user-facing messages. When a {@link StatusBar} is registered
 * (i.e. the main window is open) messages appear there unobtrusively; otherwise
 * they fall back to a standard dialog. All output is marshalled onto the EDT.
 *
 * @author Daniel Trageser
 */
public final class Notifications {

  private static StatusBar statusBar;

  private Notifications() {}

  public static void setStatusBar(StatusBar bar) {
    statusBar = bar;
  }

  public static void error(String message) {
    show(message, true);
  }

  public static void info(String message) {
    show(message, false);
  }

  private static void show(String message, boolean error) {
    Runnable task =
        () -> {
          if (statusBar != null) {
            statusBar.show(message, error);
          } else {
            JOptionPane.showMessageDialog(
                null,
                message,
                error ? "Error" : "Info",
                error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
          }
        };
    if (SwingUtilities.isEventDispatchThread()) {
      task.run();
    } else {
      SwingUtilities.invokeLater(task);
    }
  }
}
