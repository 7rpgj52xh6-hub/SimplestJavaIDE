package probe;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Standalone probe for the CheerpJ feasibility spike. It answers two questions
 * without any of the app's own code:
 *
 *   (c) which Java/JVM does CheerpJ actually provide at runtime, and
 *   (b) is the in-process JDK compiler ({@code javax.tools}) present AND able to
 *       compile a trivial class in the browser — the exact path the IDE uses to
 *       build student code.
 *
 * It then classloads and runs the freshly compiled class in THIS same JVM via a
 * URLClassLoader. That is the proposed Variant-A rearchitecture (no subprocess),
 * so a green run here proves that path is viable under CheerpJ.
 *
 * All output goes to System.out, which CheerpJ mirrors to the browser console.
 */
public class CompilerProbe {

  public static void main(String[] args) {
    line();
    System.out.println("CheerpJ compiler probe");
    line();

    // (c) What runtime are we actually on?
    System.out.println("java.version            = " + System.getProperty("java.version"));
    System.out.println("java.specification.ver  = " + System.getProperty("java.specification.version"));
    System.out.println("java.vm.name            = " + System.getProperty("java.vm.name"));
    System.out.println("java.vm.version         = " + System.getProperty("java.vm.version"));
    System.out.println("java.vendor             = " + System.getProperty("java.vendor"));
    System.out.println("java.home               = " + System.getProperty("java.home"));
    System.out.println("os.name / os.arch       = "
        + System.getProperty("os.name") + " / " + System.getProperty("os.arch"));
    line();

    // (b) Is the system compiler present?
    long t0 = System.currentTimeMillis();
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    System.out.println("ToolProvider.getSystemJavaCompiler() = "
        + (compiler == null ? "null  <-- NO COMPILER AVAILABLE" : compiler.getClass().getName()));
    if (compiler == null) {
      System.out.println("RESULT: in-browser compilation NOT possible with this runtime.");
      line();
      return;
    }
    System.out.println("Source versions supported: " + compiler.getSourceVersions());

    try {
      // Write a trivial student-style program to a writable dir.
      File work = new File("/files/probe-work");
      work.mkdirs();
      File src = new File(work, "HelloStudent.java");
      String code =
          "public class HelloStudent {\n"
        + "  record Point(int x, int y) {}\n"           // record  (Java 16+)
        + "  public static void main(String[] a) {\n"
        + "    String msg = \"\"\"\n"                    // text block (Java 15+)
        + "        Hallo aus frisch kompiliertem Code!\n"
        + "        \"\"\";\n"
        + "    System.out.print(msg);\n"
        + "    Object o = new Point(3, 4);\n"
        + "    if (o instanceof Point p) {\n"            // pattern instanceof (Java 16+)
        + "      System.out.println(\"Punkt summe = \" + (p.x() + p.y()));\n"
        + "    }\n"
        + "  }\n"
        + "}\n";
      Files.write(src.toPath(), code.getBytes(StandardCharsets.UTF_8));
      File out = new File(work, "out");
      out.mkdirs();

      // Compile exactly the way JavaCompilerService does.
      DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
      long c0 = System.currentTimeMillis();
      boolean success;
      try (StandardJavaFileManager fm =
          compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
        Iterable<? extends JavaFileObject> units =
            fm.getJavaFileObjectsFromFiles(List.of(src));
        List<String> options = List.of("-d", out.getAbsolutePath(), "-g");
        success = compiler.getTask(null, fm, diagnostics, options, null, units).call();
      }
      long c1 = System.currentTimeMillis();

      System.out.println("compile success         = " + success);
      System.out.println("compile wall time       = " + (c1 - c0) + " ms");
      for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
        System.out.println("  diag: " + d.getKind() + " @line " + d.getLineNumber()
            + " : " + d.getMessage(null));
      }
      File clazz = new File(out, "HelloStudent.class");
      System.out.println("HelloStudent.class size = "
          + (clazz.exists() ? clazz.length() + " bytes" : "MISSING"));

      if (!success) {
        System.out.println("RESULT: compiler present but compile FAILED.");
        line();
        return;
      }

      // Variant-A path: load + run the compiled class in THIS JVM (no subprocess).
      long r0 = System.currentTimeMillis();
      try (URLClassLoader cl = new URLClassLoader(new URL[] {out.toURI().toURL()},
          CompilerProbe.class.getClassLoader())) {
        Class<?> loaded = Class.forName("HelloStudent", true, cl);
        Method main = loaded.getMethod("main", String[].class);
        System.out.println("---- student program output ----");
        main.invoke(null, (Object) new String[0]);
        System.out.println("--------------------------------");
      }
      long r1 = System.currentTimeMillis();
      System.out.println("classload+run wall time = " + (r1 - r0) + " ms");
      System.out.println("TOTAL (compiler lookup + compile + run) = "
          + (System.currentTimeMillis() - t0) + " ms");
      line();
      System.out.println("RESULT: in-browser compile + in-JVM run WORKS. Variant-A path viable.");
    } catch (Throwable t) {
      System.out.println("EXCEPTION during compile/run probe:");
      t.printStackTrace(System.out);
      System.out.println("RESULT: compiler present but the compile/run path threw.");
    }
    line();
  }

  private static void line() {
    System.out.println("========================================================");
  }
}
