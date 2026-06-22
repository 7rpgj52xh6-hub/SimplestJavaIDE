package simplestJavaIDEpackage.Library.Commands;

public interface CommandListener {

  void commandOutput(String text);

  void commandFailed(Exception exp);

  /** Called once the run process has finished (whether normally or killed). */
  void commandFinished();
}
