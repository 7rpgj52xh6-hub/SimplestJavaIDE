package simplestJavaIDEpackage.Library.Commands;

public interface CommandListener {

  void commandOutput(String text);

  void commandCompleted();

  void commandFailed(Exception exp);
}
