package simplestJavaIDEpackage.Library;

import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ErrorPopupWindow;

/**
 * This class implements the terminal with all possible functions
 * 
 * @author Daniel Trageser
 * 
 */
public class Output extends JTextArea implements CommandListener {
  private static final long serialVersionUID = 4716862595957472820L;
  private Command cmd;

  public Output() {
    cmd = new Command(this);
  }

  public Command getCommand() {
    return this.cmd;
  }

  void appendText(String text) {
    this.append(text);
  }

  @Override
  public void commandOutput(String text) {
    SwingUtilities.invokeLater(new AppendTask(this, text));
  }

  @Override
  public void commandCompleted(String cmd, int result) {
    appendText("\n");
  }


  @Override
  public void commandFailed(Exception exp) {
    SwingUtilities.invokeLater(new AppendTask(this, "Command failed - " + exp.getMessage()));
  }

  private void runCommand(String command, JButton runButton, JButton compileButton)
      throws IOException, BadLocationException {
    if (!cmd.isRunning()) {
      cmd.execute(command, runButton, compileButton);
    } else {
      try {
        cmd.send(command + "\n");
      } catch (IOException ex) {
        ErrorPopupWindow.main(null, "!! Failed to send command to process:" + ex.getMessage());
      }
    }
  }

  public void compile(JTextArea outputTextPane, CodingFile codingFile, JButton runButton,
      JButton compileButton) {
    try {
      runCommand("javac " + codingFile.getAbsolutePath(), runButton, compileButton);
    } catch (IOException | BadLocationException e) {
      ErrorPopupWindow.main(null, e.getMessage());
    }
  }

  public void run(JTextArea outputTextPane, CodingFile codingFile, JButton runButton,
      JButton compileButton) {
    try {
      runCommand("java -cp " + codingFile.getClassPath() + " " + codingFile.getClassName(),
          runButton, compileButton);
    } catch (IOException | BadLocationException e) {
      ErrorPopupWindow.main(null, e.getMessage());
    }
  }

}
