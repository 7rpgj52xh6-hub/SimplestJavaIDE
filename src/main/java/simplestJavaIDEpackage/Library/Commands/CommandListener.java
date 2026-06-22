package simplestJavaIDEpackage.Library.Commands;

public interface CommandListener {

  /**
   * @param text output chunk from the running program
   * @param error true if it came from the error stream (stderr)
   */
  void commandOutput(String text, boolean error);

  void commandFailed(Exception exp);

  /** Called once the run process has finished (whether normally or killed). */
  void commandFinished();
}
