package probe;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.ServiceLoader;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

/**
 * Second probe: can the pure-Java Eclipse Compiler (ECJ) stand in for the
 * missing {@code jdk.compiler} under CheerpJ? ECJ ships a
 * {@code javax.tools.JavaCompiler} service provider, so the IDE could keep using
 * the same {@code javax.tools} API it already uses — only the compiler instance
 * would change. We load ECJ via ServiceLoader, compile a student-style class
 * (records / text blocks / pattern instanceof), then classload+run it in-JVM.
 *
 * Run with:  cheerpjRunMain("probe.EcjProbe", "/app/probe.jar:/app/ecj.jar")
 */
public class EcjProbe {

  public static void main(String[] args) throws Exception {
    line();
    System.out.println("ECJ (Eclipse Compiler) probe — java " + System.getProperty("java.version"));
    line();

    JavaCompiler compiler = null;
    for (JavaCompiler c : ServiceLoader.load(JavaCompiler.class)) {
      System.out.println("ServiceLoader found JavaCompiler: " + c.getClass().getName());
      compiler = c; // take the first (ECJ)
    }
    if (compiler == null) {
      // Fallback: instantiate directly.
      try {
        compiler = (JavaCompiler) Class
            .forName("org.eclipse.jdt.internal.compiler.tool.EclipseCompiler")
            .getDeclaredConstructor().newInstance();
        System.out.println("Direct-instantiated EclipseCompiler");
      } catch (Throwable t) {
        System.out.println("Could not obtain ECJ compiler: " + t);
        line();
        return;
      }
    }
    System.out.println("Source versions supported: " + compiler.getSourceVersions());

    File work = new File("/files/ecj-work");
    work.mkdirs();
    File src = new File(work, "HelloStudent.java");
    String code =
        "public class HelloStudent {\n"
      + "  record Point(int x, int y) {}\n"
      + "  public static void main(String[] a) {\n"
      + "    String msg = \"\"\"\n"
      + "        Hallo aus ECJ-kompiliertem Code!\n"
      + "        \"\"\";\n"
      + "    System.out.print(msg);\n"
      + "    Object o = new Point(3, 4);\n"
      + "    if (o instanceof Point p) {\n"
      + "      System.out.println(\"Punkt summe = \" + (p.x() + p.y()));\n"
      + "    }\n"
      + "  }\n"
      + "}\n";
    Files.write(src.toPath(), code.getBytes(StandardCharsets.UTF_8));
    File out = new File(work, "out");
    out.mkdirs();

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    long c0 = System.currentTimeMillis();
    boolean success;
    try (StandardJavaFileManager fm =
        compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
      Iterable<? extends JavaFileObject> units = fm.getJavaFileObjectsFromFiles(List.of(src));
      // ECJ accepts javac-style options. -source/-target 17 compiles against the
      // running JRE's platform classes (discovered via the jrt filesystem).
      List<String> options = List.of("-d", out.getAbsolutePath(), "-source", "17", "-target", "17", "-g");
      success = compiler.getTask(null, fm, diagnostics, options, null, units).call();
    }
    long c1 = System.currentTimeMillis();

    System.out.println("ECJ compile success = " + success + "   (" + (c1 - c0) + " ms)");
    for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
      System.out.println("  diag: " + d.getKind() + " @line " + d.getLineNumber()
          + " : " + d.getMessage(null));
    }
    File clazz = new File(out, "HelloStudent.class");
    System.out.println("HelloStudent.class = " + (clazz.exists() ? clazz.length() + " bytes" : "MISSING"));
    if (!success) {
      System.out.println("RESULT: ECJ present but compile FAILED (see diagnostics above).");
      line();
      return;
    }

    long r0 = System.currentTimeMillis();
    try (URLClassLoader cl = new URLClassLoader(new URL[] {out.toURI().toURL()},
        EcjProbe.class.getClassLoader())) {
      Class<?> loaded = Class.forName("HelloStudent", true, cl);
      Method main = loaded.getMethod("main", String[].class);
      System.out.println("---- student program output ----");
      main.invoke(null, (Object) new String[0]);
      System.out.println("--------------------------------");
    }
    long r1 = System.currentTimeMillis();
    System.out.println("classload+run = " + (r1 - r0) + " ms");
    line();
    System.out.println("RESULT: ECJ compile + in-JVM run WORKS under CheerpJ. Variant-A viable via ECJ.");
    line();
  }

  private static void line() {
    System.out.println("========================================================");
  }
}
