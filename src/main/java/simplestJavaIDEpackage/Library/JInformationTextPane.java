package simplestJavaIDEpackage.Library;

import javax.swing.JTextPane;

/**
 * @author Daniel Trageser
 */
public class JInformationTextPane extends JTextPane {
  private static final long serialVersionUID = 4716313567540658604L;

  public void append(String text) {
    String tmp = this.getText();
    this.setText(tmp + "\n" + text);
  }
}
