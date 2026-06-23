package simplestJavaIDEpackage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.junit.Test;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.FileManager;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedProgram;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedProgram.MethodLocation;
import simplestJavaIDEpackage.Library.CodeStructure.JavaClass;
import simplestJavaIDEpackage.Library.CodeStructure.Methods;
import simplestJavaIDEpackage.Library.Commands.CommandListener;
import simplestJavaIDEpackage.Library.Commands.CompilerHints;
import simplestJavaIDEpackage.Library.Commands.JavaCompilerService;
import simplestJavaIDEpackage.Library.Debug.DebugSession;
import simplestJavaIDEpackage.Library.Debug.TraceStep;

public class CodeGenerationTest {

  private static String javaExecutable() {
    return System.getProperty("java.home")
        + java.io.File.separator
        + "bin"
        + java.io.File.separator
        + "java";
  }

  @Test
  public void mapsGeneratedLinesBackToMethods() {
    CodingFile file = new CodingFile("Demo", "/tmp/Demo.sji");
    file.classes.get(0).methods.add(new Methods("foo", "public static void foo(){\n\t\n}"));
    GeneratedProgram program = file.buildProgram();

    // Line 1 is the class header (the "Klasse" view), method index -1.
    MethodLocation header = program.locate("Demo", 1);
    assertNotNull(header);
    assertEquals(-1, header.methodIndex());

    // The main method occupies lines 2..4; line 3 is its 2nd local line.
    MethodLocation main = program.locate("Demo", 3);
    assertNotNull(main);
    assertEquals("Main Method", main.methodName());
    assertEquals(2, main.localLine());

    // foo occupies lines 5..7; line 6 is its 2nd local line.
    MethodLocation foo = program.locate("Demo", 6);
    assertNotNull(foo);
    assertEquals("foo", foo.methodName());
    assertEquals(2, foo.localLine());
  }

  @Test
  public void mapsCompilerErrorToMethodAndLine() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    CodingFile file = new CodingFile("Demo", dir.resolve("Demo.sji").toString());
    file.classes
        .get(0)
        .methods
        .set(
            0,
            new Methods(
                "Main Method", "public static void main(String[] args){\n\tundefinedCall();\n}"));
    GeneratedProgram program = file.buildProgram();
    file.writeSources();

    JavaCompilerService.Result result =
        JavaCompilerService.compile(file.javaFilePaths(), dir.toString());
    assertTrue(result.compilerAvailable());
    assertFalse(result.success());

    MethodLocation firstError = null;
    for (Diagnostic<? extends JavaFileObject> diagnostic : result.diagnostics()) {
      if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
        firstError = program.locate("Demo", (int) diagnostic.getLineNumber());
        break;
      }
    }
    assertNotNull(firstError);
    assertEquals("Main Method", firstError.methodName());
    assertEquals(2, firstError.localLine());
  }

  @Test
  public void commonCompilerErrorHasFriendlyHint() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    CodingFile file = new CodingFile("Demo", dir.resolve("Demo.sji").toString());
    file.classes
        .get(0)
        .methods
        .set(
            0,
            new Methods(
                "Main Method", "public static void main(String[] args){\n\tundefinedCall();\n}"));
    file.writeSources();

    JavaCompilerService.Result result =
        JavaCompilerService.compile(file.javaFilePaths(), dir.toString());

    String hint = null;
    for (Diagnostic<? extends JavaFileObject> diagnostic : result.diagnostics()) {
      if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
        hint = CompilerHints.friendlyHint(diagnostic);
        break;
      }
    }
    assertNotNull("expected a beginner hint for an unknown-symbol error", hint);
  }

  @Test
  public void compilesAndRunsHelloWorld() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    CodingFile file = new CodingFile("Demo", dir.resolve("Demo.sji").toString());
    file.writeSources(); // default main prints "Hello World"

    assertTrue(JavaCompilerService.compile(file.javaFilePaths(), dir.toString()).success());

    Process process =
        new ProcessBuilder(javaExecutable(), "-cp", dir.toString(), file.entryClassName())
            .redirectErrorStream(true)
            .start();
    String output = new String(process.getInputStream().readAllBytes());
    process.waitFor();
    assertTrue("expected greeting, got: " + output, output.contains("Hello World"));
  }

  @Test
  public void compilesAndRunsMultipleClasses() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    CodingFile file = new CodingFile("Main", dir.resolve("Main.sji").toString());
    file.expertMode = true;
    file.classes
        .get(0)
        .methods
        .set(
            0,
            new Methods(
                "Main Method",
                "public static void main(String[] args){\n"
                    + "\tGreeter g = new Greeter();\n"
                    + "\tg.hello();\n"
                    + "}"));
    JavaClass greeter = new JavaClass("Greeter");
    greeter.methods.add(
        new Methods("hello", "public void hello(){\n\tSystem.out.println(\"Hi from Greeter\");\n}"));
    file.classes.add(greeter);
    file.writeSources();

    assertTrue(JavaCompilerService.compile(file.javaFilePaths(), dir.toString()).success());
    assertEquals("Main", file.entryClassName());

    Process process =
        new ProcessBuilder(javaExecutable(), "-cp", dir.toString(), file.entryClassName())
            .redirectErrorStream(true)
            .start();
    String output = new String(process.getInputStream().readAllBytes());
    process.waitFor();
    assertTrue("expected greeting, got: " + output, output.contains("Hi from Greeter"));
  }

  @Test
  public void steppingCapturesVariables() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    CodingFile file = new CodingFile("Demo", dir.resolve("Demo.sji").toString());
    file.classes
        .get(0)
        .methods
        .set(
            0,
            new Methods(
                "Main Method",
                "public static void main(String[] args){\n"
                    + "\tint summe = 0;\n"
                    + "\tfor (int i = 1; i <= 3; i++) {\n"
                    + "\t\tsumme = summe + i;\n"
                    + "\t}\n"
                    + "\tSystem.out.println(summe);\n"
                    + "}"));
    file.writeSources();
    assertTrue(JavaCompilerService.compile(file.javaFilePaths(), dir.toString()).success());

    List<TraceStep> steps = debugToEnd("Demo", dir.toString(), null);
    assertTrue(
        "expected summe to reach 6 while stepping",
        steps.stream().anyMatch(s -> "6".equals(s.variables().get("summe"))));
  }

  @Test
  public void steppingWithInputAppliesTheValue() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    CodingFile file = new CodingFile("Demo", dir.resolve("Demo.sji").toString());
    file.imports.add("java.util.Scanner");
    file.classes
        .get(0)
        .methods
        .set(
            0,
            new Methods(
                "Main Method",
                "public static void main(String[] args){\n"
                    + "\tScanner sc = new Scanner(System.in);\n"
                    + "\tint x = sc.nextInt();\n"
                    + "\tSystem.out.println(x);\n"
                    + "}"));
    file.writeSources();
    assertTrue(JavaCompilerService.compile(file.javaFilePaths(), dir.toString()).success());

    List<TraceStep> steps = debugToEnd("Demo", dir.toString(), "42\n");
    assertTrue(
        "expected the provided input 42 to appear while stepping",
        steps.stream().anyMatch(s -> "42".equals(s.variables().get("x"))));
  }

  /** Drives a live debug session to the end, auto-stepping and collecting each step. */
  private List<TraceStep> debugToEnd(String className, String classpath, String firstInput)
      throws InterruptedException {
    List<TraceStep> collected = Collections.synchronizedList(new ArrayList<>());
    CountDownLatch done = new CountDownLatch(1);
    DebugSession[] ref = new DebugSession[1];
    boolean[] wrote = {false};
    CommandListener silent =
        new CommandListener() {
          @Override
          public void commandOutput(String text, boolean error) {}

          @Override
          public void commandFailed(Exception exp) {}

          @Override
          public void commandFinished() {}
        };
    DebugSession.Listener driver =
        new DebugSession.Listener() {
          @Override
          public void onPaused(TraceStep step) {
            collected.add(step);
            if (firstInput != null && !wrote[0]) {
              wrote[0] = true;
              ref[0].write(firstInput);
            }
            ref[0].step();
          }

          @Override
          public void onFinished(boolean truncated) {
            done.countDown();
          }

          @Override
          public void onError(String message) {
            done.countDown();
          }
        };
    DebugSession session = new DebugSession(className, classpath, 2000, silent, driver);
    ref[0] = session;
    session.start();
    assertTrue("debug session did not finish in time", done.await(25, TimeUnit.SECONDS));
    return collected;
  }

  @Test
  public void migratesLegacySingleClassFile() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    Path file = dir.resolve("Old.sji");
    String legacy =
        "{\"imports\":[\"java.util.Scanner\"],"
            + "\"methods\":[{\"name\":\"Main Method\",\"content\":\"public static void main(String[] a){}\"}],"
            + "\"javaClass\":{\"className\":\"Old\"}}";
    Files.writeString(file, legacy);

    CodingFile loaded = FileManager.load("Old", file.toString(), false);
    assertNotNull(loaded);
    assertEquals(1, loaded.classes.size());
    assertEquals("Old", loaded.classes.get(0).name);
    assertEquals("Main Method", loaded.classes.get(0).methods.get(0).name());
    assertEquals(List.of("java.util.Scanner"), loaded.imports);
  }

  @Test
  public void jsonRoundTripPreservesContent() throws Exception {
    Path dir = Files.createTempDirectory("sji");
    String path = dir.resolve("Demo.sji").toString();
    CodingFile original = new CodingFile("Demo", path);
    original.imports.add("java.util.Scanner");
    original.classes.get(0).methods.add(new Methods("foo", "public static void foo(){}"));
    assertTrue(FileManager.save(original));

    CodingFile loaded = FileManager.load("Demo", path, false);
    assertNotNull(loaded);
    assertEquals(original.classes.get(0).name, loaded.classes.get(0).name);
    assertEquals(original.imports, loaded.imports);
    assertEquals(original.classes.get(0).methods, loaded.classes.get(0).methods);
    // The path is not serialized; it is rebound to where the file was loaded from.
    assertEquals(path, loaded.getFilepath());
  }
}
