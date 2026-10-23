package simplestJavaIDEpackage.Library.Commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the student's compiled program IN THIS JVM (no subprocess) — the only way
 * under CheerpJ, which cannot spawn a second process. A fresh {@link URLClassLoader}
 * loads the entry class from the compiled-output classpath, {@code main} runs on a
 * daemon thread, and {@code System.out}/{@code err}/{@code in} are redirected for
 * the run so output reaches the terminal and the input field can feed stdin.
 *
 * <p>Limitation (addressed in a later step): a student {@code System.exit(...)} or a
 * tight endless loop are not yet contained — {@link #kill()} is best-effort
 * (interrupt + closing stdin), which stops blocking calls but not a pure CPU loop.
 *
 * @author Daniel Trageser
 */
public class InJvmProgramRunner implements ProgramRunner {

  private final CommandListener listener;
  private final String entryClass;
  private final String classpath;
  private final Thread thread;
  private volatile boolean running = true;
  private volatile PipedOutputStream stdinSink;

  public InJvmProgramRunner(CommandListener listener, String entryClass, String classpath) {
    this.listener = listener;
    this.entryClass = entryClass;
    this.classpath = classpath;
    this.thread = new Thread(this::execute, "student-program");
    this.thread.setDaemon(true);
    this.thread.start();
  }

  private void execute() {
    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;
    InputStream originalIn = System.in;
    PipedInputStream stdin = new PipedInputStream(1 << 16);
    PrintStream out = new PrintStream(new ListenerOutputStream(listener, false), true, StandardCharsets.UTF_8);
    PrintStream err = new PrintStream(new ListenerOutputStream(listener, true), true, StandardCharsets.UTF_8);
    try {
      stdinSink = new PipedOutputStream(stdin);
      System.setOut(out);
      System.setErr(err);
      System.setIn(stdin);

      URLClassLoader loader = new URLClassLoader(classpathUrls(), getClass().getClassLoader());
      try {
        Class<?> clazz = Class.forName(entryClass, true, loader);
        Method main = clazz.getDeclaredMethod("main", String[].class);
        main.setAccessible(true);
        main.invoke(null, (Object) new String[0]);
      } catch (InvocationTargetException e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        cause.printStackTrace(err); // to the terminal; stack trace gets annotated there
      } catch (Throwable t) {
        t.printStackTrace(err);
      } finally {
        try { loader.close(); } catch (IOException ignore) { /* best effort */ }
      }
      out.flush();
      err.flush();
    } catch (IOException e) {
      listener.commandFailed(e);
    } finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
      System.setIn(originalIn);
      closeQuietly(stdinSink);
      closeQuietly(stdin);
      running = false;
      listener.commandFinished();
    }
  }

  private URL[] classpathUrls() throws IOException {
    List<URL> urls = new ArrayList<>();
    for (String entry : classpath.split(File.pathSeparator)) {
      if (!entry.isBlank()) {
        urls.add(new File(entry).toURI().toURL());
      }
    }
    return urls.toArray(new URL[0]);
  }

  @Override
  public void write(String text) throws IOException {
    PipedOutputStream sink = stdinSink;
    if (sink != null && running) {
      sink.write(text.getBytes(StandardCharsets.UTF_8));
      sink.flush();
    }
  }

  @Override
  public boolean isRunning() {
    return running && thread.isAlive();
  }

  @Override
  public void kill() {
    running = false;
    thread.interrupt();
    closeQuietly(stdinSink); // unblocks a Scanner waiting on input
  }

  private static void closeQuietly(java.io.Closeable c) {
    if (c != null) {
      try { c.close(); } catch (IOException ignore) { /* best effort */ }
    }
  }

  /** Forwards everything written to it to the {@link CommandListener}. */
  private static final class ListenerOutputStream extends OutputStream {
    private final CommandListener listener;
    private final boolean error;

    ListenerOutputStream(CommandListener listener, boolean error) {
      this.listener = listener;
      this.error = error;
    }

    @Override
    public void write(int b) {
      listener.commandOutput(new String(new byte[] {(byte) b}, StandardCharsets.UTF_8), error);
    }

    @Override
    public void write(byte[] b, int off, int len) {
      listener.commandOutput(new String(b, off, len, StandardCharsets.UTF_8), error);
    }
  }
}
