package simplestJavaIDEpackage.Library;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.swing.JTextPane;

/**
 * @author Daniel Trageser
 */
public class InformationTextPane extends JTextPane {
  private static final long serialVersionUID = 4716313567540658604L;

  /**
   * Appends text with date and time
   * 
   * @param text is the text that shall be added to the information text pane
   */
  public void append(String text) {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    String tmp = this.getText();
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    this.setText("[" + sdf.format(timestamp) + "]: " + tmp + "\n" + text);
  }
}
