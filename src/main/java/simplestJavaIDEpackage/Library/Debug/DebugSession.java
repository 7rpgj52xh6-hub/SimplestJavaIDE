package simplestJavaIDEpackage.Library.Debug;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.CharValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import simplestJavaIDEpackage.Library.Commands.CommandListener;
import simplestJavaIDEpackage.Library.Commands.StreamReader;

/**
 * Interactive, live step debugger built on the Java Debug Interface. The program
 * is launched suspended and advances one source line per {@link #step()} call:
 * output appears and input is read <em>as you step</em> (not all up front). The
 * student feeds input through {@link #write}. One step skips consecutive stops on
 * the same line (e.g. a loop header) so "Next" always moves to a new line.
 *
 * @author Daniel Trageser
 */
public class DebugSession {

  /** Callbacks are invoked on the session thread; the UI marshals them to the EDT. */
  public interface Listener {
    void onPaused(TraceStep step);

    void onFinished(boolean truncated);

    void onError(String message);
  }

  private enum Command {
    STEP,
    CONTINUE,
    STOP
  }

  private static final long CONTINUE_WALL_TIME_MS = 15_000;

  private final String className;
  private final String classpath;
  private final int maxSteps;
  private final CommandListener output;
  private final Listener listener;
  private final BlockingQueue<Command> commands = new LinkedBlockingQueue<>();
  private volatile boolean stopRequested;
  private volatile VirtualMachine vm;

  public DebugSession(
      String className,
      String classpath,
      int maxSteps,
      CommandListener output,
      Listener listener) {
    this.className = className;
    this.classpath = classpath;
    this.maxSteps = maxSteps;
    this.output = output;
    this.listener = listener;
  }

  public void start() {
    Thread thread = new Thread(this::run, "debug-session");
    thread.setDaemon(true);
    thread.start();
  }

  /** Advance to the next source line. */
  public void step() {
    commands.add(Command.STEP);
  }

  /** Run the rest of the program without stepping. */
  public void continueToEnd() {
    commands.add(Command.CONTINUE);
  }

  public void stop() {
    stopRequested = true;
    commands.add(Command.STOP);
  }

  /** Sends a line of input to the running program. */
  public void write(String text) {
    VirtualMachine current = vm;
    if (current == null) {
      return;
    }
    try {
      current.process().getOutputStream().write(text.getBytes(StandardCharsets.UTF_8));
      current.process().getOutputStream().flush();
    } catch (Exception ignored) {
      // process already gone
    }
  }

  private void run() {
    boolean truncated = false;
    try {
      LaunchingConnector connector = Bootstrap.virtualMachineManager().defaultConnector();
      Map<String, Connector.Argument> args = connector.defaultArguments();
      args.get("main").setValue(className);
      args.get("options").setValue("-cp \"" + classpath + "\"");
      vm = connector.launch(args);

      new StreamReader(output, vm.process().getInputStream(), false);
      new StreamReader(output, vm.process().getErrorStream(), true);

      EventQueue queue = vm.eventQueue();
      EventRequestManager requests = vm.eventRequestManager();
      int lastPausedLine = -1;
      int stepCount = 0;
      long continueDeadline = Long.MAX_VALUE;
      boolean running = true;

      while (running) {
        EventSet set;
        try {
          set = queue.remove(200);
        } catch (VMDisconnectedException e) {
          break;
        }
        if (stopRequested) {
          break;
        }
        if (set == null) {
          if (System.currentTimeMillis() > continueDeadline) {
            truncated = true;
            break;
          }
          continue;
        }

        boolean paused = false;
        for (Event event : set) {
          if (event instanceof VMStartEvent start) {
            StepRequest step =
                requests.createStepRequest(
                    start.thread(), StepRequest.STEP_LINE, StepRequest.STEP_INTO);
            step.addClassFilter(className);
            step.enable();
          } else if (event instanceof StepEvent stepEvent) {
            int line = stepEvent.location().lineNumber();
            if (line != lastPausedLine) {
              lastPausedLine = line;
              stepCount++;
              listener.onPaused(capture(stepEvent));
              paused = true;
            }
          } else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
            running = false;
          }
        }
        if (!running) {
          break;
        }

        if (paused) {
          if (stepCount >= maxSteps) {
            truncated = true;
            break;
          }
          Command command = awaitCommand();
          if (command == Command.STOP) {
            running = false;
            break;
          }
          if (command == Command.CONTINUE) {
            for (StepRequest request : requests.stepRequests()) {
              request.disable();
            }
            continueDeadline = System.currentTimeMillis() + CONTINUE_WALL_TIME_MS;
          }
        }
        try {
          set.resume();
        } catch (VMDisconnectedException e) {
          break;
        }
      }
    } catch (Exception e) {
      listener.onError("Debugger-Fehler: " + e.getMessage());
    } finally {
      try {
        if (vm != null) {
          vm.exit(0);
        }
      } catch (Exception ignored) {
        // already gone
      }
      listener.onFinished(truncated);
    }
  }

  private Command awaitCommand() {
    while (true) {
      if (stopRequested) {
        return Command.STOP;
      }
      try {
        Command command = commands.poll(150, TimeUnit.MILLISECONDS);
        if (command != null) {
          return command;
        }
      } catch (InterruptedException e) {
        return Command.STOP;
      }
    }
  }

  private TraceStep capture(StepEvent event) {
    try {
      ThreadReference thread = event.thread();
      if (thread.frameCount() == 0) {
        return new TraceStep(event.location().lineNumber(), Map.of());
      }
      StackFrame frame = thread.frame(0);
      Map<String, String> variables = new LinkedHashMap<>();
      try {
        for (LocalVariable variable : frame.visibleVariables()) {
          variables.put(variable.name(), format(frame.getValue(variable)));
        }
      } catch (com.sun.jdi.AbsentInformationException ignored) {
        // compiled without -g
      }
      return new TraceStep(event.location().lineNumber(), variables);
    } catch (IncompatibleThreadStateException e) {
      return new TraceStep(event.location().lineNumber(), Map.of());
    }
  }

  private static String format(Value value) {
    if (value == null) {
      return "null";
    }
    if (value instanceof StringReference string) {
      return "\"" + string.value() + "\"";
    }
    if (value instanceof CharValue character) {
      return "'" + character.value() + "'";
    }
    if (value instanceof ArrayReference array) {
      int length = array.length();
      int shown = Math.min(length, 20);
      StringBuilder builder = new StringBuilder("[");
      for (int i = 0; i < shown; i++) {
        if (i > 0) {
          builder.append(", ");
        }
        builder.append(format(array.getValue(i)));
      }
      if (length > shown) {
        builder.append(", …");
      }
      return builder.append("]").toString();
    }
    if (value instanceof PrimitiveValue) {
      return value.toString();
    }
    if (value instanceof ObjectReference object) {
      return object.referenceType().name();
    }
    return value.toString();
  }
}
