package simplestJavaIDEpackage;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * A thin status line at the bottom of the main window. Replaces the old
 * always-on-top error popups with a calm, non-blocking place for messages that
 * fade away on their own.
 *
 * @author Daniel Trageser
 */
public class StatusBar extends JPanel {

  private static final Color ERROR_COLOR = new Color(232, 104, 104);
  private final JLabel label = new JLabel(" ");
  private final Color defaultForeground;
  private final Timer clearTimer;

  public StatusBar() {
    super(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
    Color line = UIManager.getColor("Separator.foreground");
    if (line != null) {
      setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createMatteBorder(1, 0, 0, 0, line),
              BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    }
    defaultForeground = label.getForeground();
    add(label, BorderLayout.WEST);
    clearTimer = new Timer(6000, e -> clear());
    clearTimer.setRepeats(false);
  }

  public void show(String message, boolean error) {
    label.setForeground(error ? ERROR_COLOR : defaultForeground);
    label.setText(message == null ? " " : message);
    clearTimer.restart();
  }

  private void clear() {
    label.setForeground(defaultForeground);
    label.setText(" ");
  }
}
