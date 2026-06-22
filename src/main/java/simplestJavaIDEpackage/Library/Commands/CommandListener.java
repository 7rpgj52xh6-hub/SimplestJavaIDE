package simplestJavaIDEpackage.Library.Commands;

public interface CommandListener {

  void commandOutput(String text);

  void commandFailed(Exception exp);
}
