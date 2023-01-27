package simplestJavaIDEpackage.Library.Commands;

public interface CommandListener {

  public void commandOutput(String text);

  public void commandFailed(Exception exp);

  public void compileFailed();

  public void compileSuccessful();
}
