package simplestJavaIDEpackage.Library.Terminal;

import simplestJavaIDEpackage.Library.Output;

public class AppendTask implements Runnable {

  private Output terminal;
  private String text;

  public AppendTask(Output textArea, String text) {
    this.terminal = textArea;
    this.text = text;
  }

  @Override
  public void run() {
    terminal.appendText(text);
  }
}
