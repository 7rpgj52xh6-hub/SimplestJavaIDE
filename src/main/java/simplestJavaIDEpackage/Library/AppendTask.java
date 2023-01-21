package simplestJavaIDEpackage.Library;

public class AppendTask implements Runnable {

  private TerminalTextPane terminal;
  private String text;

  public AppendTask(TerminalTextPane textArea, String text) {
    this.terminal = textArea;
    this.text = text;
  }

  @Override
  public void run() {
    terminal.appendText(text);
  }
}
