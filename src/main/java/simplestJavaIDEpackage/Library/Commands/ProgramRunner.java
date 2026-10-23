package simplestJavaIDEpackage.Library.Commands;

import java.io.IOException;

/**
 * A running student program the terminal can feed input to, query and stop.
 * Two implementations: {@link Runner} (Desktop, a separate OS process) and
 * {@link InJvmProgramRunner} (Web/CheerpJ, in this same JVM — no process).
 *
 * @author Daniel Trageser
 */
public interface ProgramRunner {

  /** Sends a line of input to the program's stdin. */
  void write(String text) throws IOException;

  /** True while the program is still running. */
  boolean isRunning();

  /** Stops the program (best-effort). */
  void kill();
}
