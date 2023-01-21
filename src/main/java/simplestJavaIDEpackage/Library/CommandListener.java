package simplestJavaIDEpackage.Library;

public interface CommandListener {

  void commandOutput(String text);

  void commandCompleted(String cmd, int result);

  void commandFailed(Exception exp);

}
