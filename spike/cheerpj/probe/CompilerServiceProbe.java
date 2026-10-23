package probe;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import simplestJavaIDEpackage.Platform;
import simplestJavaIDEpackage.Library.Commands.JavaCompilerService;

/**
 * Exercises the REAL app class {@link JavaCompilerService} (not a copy) under
 * CheerpJ, to confirm step-2b: the web back-end compiles student code and returns
 * structured {@link Diagnostic}s whose source/line/kind/code are what the IDE's
 * error-mapping consumes.
 *
 * Run: cheerpjRunMain("probe.CompilerServiceProbe", "/app/probe-csp.jar:/app/SimplestJavaIDE-web.jar")
 */
public class CompilerServiceProbe {

  public static void main(String[] args) throws Exception {
    System.out.println("Platform.WEB = " + Platform.WEB + "   vm=" + System.getProperty("java.vm.name"));
    File work = new File("/files/csp-work");
    work.mkdirs();
    File out = new File(work, "out");
    out.mkdirs();

    test(work, out, "ExampleProgramm",
        "public class ExampleProgramm {\n"
      + "  public static void main(String[] args){\n"
      + "    int x = 5\n"                              // error: missing ';' (line 3)
      + "    System.out.println(x);\n"
      + "  }\n}\n");

    test(work, out, "ExampleProgramm",
        "import java.util.Scanner;\n"
      + "public class ExampleProgramm {\n"
      + "  public static void main(String[] args){\n"
      + "    Scanner sc = new Scanner(System.in);\n"
      + "    System.out.println(\"Hallo\");\n"
      + "  }\n}\n");
  }

  private static void test(File work, File out, String cls, String code) throws Exception {
    File src = new File(work, cls + ".java");
    Files.write(src.toPath(), code.getBytes(StandardCharsets.UTF_8));
    File[] old = out.listFiles();
    if (old != null) for (File f : old) f.delete();

    long t0 = System.currentTimeMillis();
    JavaCompilerService.Result r =
        JavaCompilerService.compile(List.of(src.getAbsolutePath()), out.getAbsolutePath());
    long t1 = System.currentTimeMillis();

    System.out.println("\ncompile " + cls + ": available=" + r.compilerAvailable()
        + " success=" + r.success() + " diags=" + r.diagnostics().size()
        + "  (" + (t1 - t0) + " ms)");
    for (Diagnostic<? extends JavaFileObject> d : r.diagnostics()) {
      String srcName = d.getSource() == null ? "null" : new File(d.getSource().getName()).getName();
      System.out.println("  kind=" + d.getKind() + " src=" + srcName
          + " line=" + d.getLineNumber() + " code=" + d.getCode()
          + " msg=" + d.getMessage(null));
    }
    System.out.println("  " + cls + ".class = "
        + (new File(out, cls + ".class").exists() ? "present" : "missing"));
  }
}
