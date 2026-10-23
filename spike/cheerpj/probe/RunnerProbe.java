package probe;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import simplestJavaIDEpackage.Platform;
import simplestJavaIDEpackage.Library.Commands.CommandListener;
import simplestJavaIDEpackage.Library.Commands.InJvmProgramRunner;
import simplestJavaIDEpackage.Library.Commands.JavaCompilerService;
import simplestJavaIDEpackage.Library.Commands.ProgramRunner;

/**
 * Exercises step-5 end to end under CheerpJ with the REAL app classes: compile a
 * Scanner program via {@link JavaCompilerService} (web), run it via
 * {@link InJvmProgramRunner} in this JVM, feed stdin, and confirm output + the
 * finished callback. The listener writes to the ORIGINAL stdout (captured before
 * the run) to avoid recursing through the runner's stdout redirection.
 *
 * Run: cheerpjRunMain("probe.RunnerProbe", "/app/combined-run.jar")
 */
public class RunnerProbe {

  private static volatile boolean finished = false;

  public static void main(String[] args) throws Exception {
    PrintStream real = System.out; // bypasses the runner's System.out redirect
    real.println("Platform.WEB = " + Platform.WEB);

    File work = new File("/files/runner-work");
    work.mkdirs();
    File out = new File(work, "out");
    out.mkdirs();

    String code =
        "import java.util.Scanner;\n"
      + "public class ExampleProgramm {\n"
      + "  public static void main(String[] args){\n"
      + "    Scanner sc = new Scanner(System.in);\n"
      + "    System.out.println(\"Wie heißt du?\");\n"
      + "    String name = sc.nextLine();\n"
      + "    System.out.println(\"Hallo \" + name + \"!\");\n"
      + "    System.out.println(\"1 + 2 = \" + (1 + 2));\n"
      + "  }\n}\n";
    File src = new File(work, "ExampleProgramm.java");
    Files.write(src.toPath(), code.getBytes(StandardCharsets.UTF_8));

    JavaCompilerService.Result r =
        JavaCompilerService.compile(List.of(src.getAbsolutePath()), out.getAbsolutePath());
    real.println("compile success=" + r.success() + " diags=" + r.diagnostics().size());
    if (!r.success()) {
      return;
    }

    CommandListener listener =
        new CommandListener() {
          @Override public void commandOutput(String text, boolean error) {
            real.print((error ? "[ERR] " : "") + text);
          }
          @Override public void commandFailed(Exception exp) {
            real.println("[FAILED] " + exp);
          }
          @Override public void commandFinished() {
            real.println("[FINISHED]");
            finished = true;
          }
        };

    real.println("---- run (waiting for prompt) ----");
    ProgramRunner runner = new InJvmProgramRunner(listener, "ExampleProgramm", out.getAbsolutePath());
    Thread.sleep(600); // let it print the prompt and block on nextLine()
    real.println("\n---- sending input: Welt ----");
    runner.write("Welt\n");

    for (int i = 0; i < 50 && !finished; i++) {
      Thread.sleep(100);
    }
    real.println("finished=" + finished + " stillRunning=" + runner.isRunning());
  }
}
