package simplestJavaIDEpackage.Library.Commands;

public interface CommandListener {

  void commandOutput(String text);

  void commandFailed(Exception exp);

  void compileFailed();

  void compileSuccessful();
}
