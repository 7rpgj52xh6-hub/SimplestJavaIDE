package simplestJavaIDEpackage.Library;

import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import simplestJavaIDEpackage.CodingFile;

public class TerminalTextPane extends JTextArea implements CommandListener {
  /**
   * 
   */
  private static final long serialVersionUID = 4716862595957472820L;
  public Command cmd;

  public TerminalTextPane() {
    cmd = new Command(this);
  }

  public void appendText(String text) {
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

  public void runCommand(String command, JButton runButton, JButton compileButton)
      throws IOException, BadLocationException {
    if (!cmd.isRunning()) {
      cmd.execute(command, runButton, compileButton);
    } else {
      try {
        cmd.send(command + "\n");
      } catch (IOException ex) {
        // this.getInformationTextPane()
        // .append("!! Failed to send command to process:" + ex.getMessage());
        // TODO Error handling
      }
    }
  }

  public void compile(JTextArea outputTextPane, CodingFile codingFile, JButton runButton,
      JButton compileButton) {
    try {
      runCommand("javac " + codingFile.getAbsolutePath(), runButton, compileButton);
    } catch (IOException | BadLocationException e) {
      // this.getInformationTextPane().append(e.getMessage());
      // TODO Error handling
    }
  }

  public void runApplication(JTextArea outputTextPane, CodingFile codingFile, JButton runButton,
      JButton compileButton) {
    try {
      runCommand("java -cp " + codingFile.getClassPath() + " " + codingFile.getClassName(),
          runButton, compileButton);
    } catch (IOException | BadLocationException e) {
      // this.getInformationTextPane().append(e.getMessage());
      // TODO Error Handling
    }
  }

}
