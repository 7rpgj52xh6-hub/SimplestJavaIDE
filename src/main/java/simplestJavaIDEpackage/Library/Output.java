package simplestJavaIDEpackage.Library;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.Terminal.AppendTask;
import simplestJavaIDEpackage.Library.Terminal.Command;
import simplestJavaIDEpackage.Library.Terminal.CommandListener;
import simplestJavaIDEpackage.Library.Terminal.CompileRunnable;
import simplestJavaIDEpackage.Library.Terminal.RunRunnable;

/**
 * This class implements the terminal with all possible functions
 * 
 * @author Daniel Trageser
 * 
 */
public class Output extends JScrollPane implements CommandListener {
  private static final long serialVersionUID = 4716862595957472820L;
  private Command cmd;
  private JTextArea terminalTextArea;
  private JTextField userInputField;

  public enum CommandType {
    COMPILE_AND_RUN, INPUT
  }

  public Output(JTextField userInputField) {
    cmd = new Command(this);
    terminalTextArea = new JTextArea();
    DefaultCaret terminalTextAreaCaret = (DefaultCaret) terminalTextArea.getCaret();
    terminalTextAreaCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    this.userInputField = userInputField;
    this.setViewportView(terminalTextArea);
    this.setFocusable(false);
    this.getTextArea().setEditable(false);
    this.getTextArea().setBackground(new Color(35, 35, 35));
  }

  public JTextArea getTextArea() {
    return this.terminalTextArea;
  }

  // TODO Make obsolete
  public Command getCommand() {
    return this.cmd;
  }

  public void appendText(String text) {
    this.getTextArea().append(text);
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
    ErrorPopupWindow.throwMessage("Command failed - " + exp.getMessage());
  }

  /**
   * Sends compile or run command to system terminal. Returns true if errors occurred
   * 
   * @param ct defines if compile or run
   * @param cf is used for the path that is used for the commands
   */
  public void run(CommandType ct, CodingFile cf, JButton runButton) {
    switch (ct) {
      case COMPILE_AND_RUN:
        SwingUtilities.invokeLater(new CompileRunnable(cmd, runButton, cf));
        break;
      case INPUT:
        String input = userInputField.getText();
        if (input != null) {
          SwingUtilities.invokeLater(new RunRunnable(cmd, input, runButton));
        }
        break;
      default:
        ErrorPopupWindow.throwMessage("False command. Not allowed.");
        break;
    }
  }
}
