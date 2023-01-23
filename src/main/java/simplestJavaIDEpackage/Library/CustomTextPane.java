package simplestJavaIDEpackage.Library;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextPane;

/**
 * Modified JTextPane to be able to append text with a timestamp
 * 
 * @author Daniel Trageser
 */
public class CustomTextPane extends JTextPane {
  private static final long serialVersionUID = 4716313567540658604L;
  private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
  List<String> lines = new ArrayList<String>();

  /**
   * Appends text with date and time
   * 
   * @param text is the text that shall be added to the information text pane
   */
  public void append(String text) {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    lines.add("[" + sdf.format(timestamp) + "]:\n\t" + text);
    String output = "";
    for (int i = lines.size(); i > 0; i--) {
      output = output + lines.get(i - 1) + "\n";
    }
    this.setText(output);
  }
}
