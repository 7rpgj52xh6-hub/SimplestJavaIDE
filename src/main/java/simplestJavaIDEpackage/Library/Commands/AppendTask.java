package simplestJavaIDEpackage.Library.Commands;

import simplestJavaIDEpackage.Library.TerminalPanel;

public class AppendTask implements Runnable {

  private TerminalPanel terminal;
  private String text;

  public AppendTask(TerminalPanel textArea, String text) {
    this.terminal = textArea;
    this.text = text;
  }

  @Override
  public void run() {
    terminal.appendText(text);
  }
}
