package probe;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Third probe: ECJ's StandardFileManager and its default batch FileSystem both
 * try to open the running JDK's jrt module image, which CheerpJ does not provide.
 * So we drive ECJ's BATCH compiler and try to point it at a BUNDLED platform jar
 * instead. This probe tries several option-sets and reports which (if any) works.
 *
 *   S1: --system none -classpath rt17.jar        (goal: full Java 17 language)
 *   S2: -source 1.8 -target 1.8 -bootclasspath rt8.jar  (Java 8 language fallback)
 *
 * Run with: cheerpjRunMain("probe.EcjBatchProbe", "/app/probe.jar:/app/ecj.jar")
 */
public class EcjBatchProbe {

  public static void main(String[] args) throws Exception {
    line();
    System.out.println("ECJ batch-compiler probe — java " + System.getProperty("java.version"));
    line();

    File work = new File("/files/ecjb-work");
    work.mkdirs();

    // Java-17-flavoured student source (records, text blocks, pattern instanceof).
    String code17 =
        "import java.util.List;\n"
      + "public class HelloStudent {\n"
      + "  record Point(int x, int y) {}\n"
      + "  public static void main(String[] a) {\n"
      + "    String msg = \"\"\"\n        Hallo (Java17)!\n        \"\"\";\n"
      + "    System.out.print(msg);\n"
      + "    Object o = new Point(3, 4);\n"
      + "    if (o instanceof Point p) System.out.println(\"summe=\" + (p.x()+p.y()));\n"
      + "    System.out.println(\"Liste: \" + List.of(1,2,3));\n"
      + "  }\n}\n";
    // Plain Java-8 student source (no modern features).
    String code8 =
        "import java.util.ArrayList;\n"
      + "public class Hello8 {\n"
      + "  public static void main(String[] a) {\n"
      + "    ArrayList<String> l = new ArrayList<>();\n"
      + "    l.add(\"Hallo (Java8)!\");\n"
      + "    for (String s : l) System.out.println(s);\n"
      + "  }\n}\n";

    tryStrategy("S1  --system none -classpath rt17.jar (Java 17)", work, "HelloStudent", code17,
        new String[] {"-source", "17", "-target", "17", "--system", "none",
            "-classpath", "/app/rt17.jar"});

    tryStrategy("S2  -bootclasspath rt8.jar (Java 8)", work, "Hello8", code8,
        new String[] {"-source", "1.8", "-target", "1.8", "-bootclasspath", "/app/rt8.jar"});

    line();
  }

  private static void tryStrategy(String label, File work, String cls, String code, String[] extra)
      throws Exception {
    System.out.println("\n### " + label);
    File dir = new File(work, cls);
    dir.mkdirs();
    File out = new File(dir, "out");
    out.mkdirs();
    File src = new File(dir, cls + ".java");
    Files.write(src.toPath(), code.getBytes(StandardCharsets.UTF_8));

    java.util.List<String> cmd = new java.util.ArrayList<>();
    for (String e : extra) cmd.add(e);
    cmd.add("-proc:none");
    cmd.add("-nowarn");
    cmd.add("-d");
    cmd.add(out.getAbsolutePath());
    cmd.add(src.getAbsolutePath());
    String cmdLine = String.join(" ", cmd);
    System.out.println("ecj: " + cmdLine);

    StringWriter so = new StringWriter(), se = new StringWriter();
    long t0 = System.currentTimeMillis();
    boolean ok;
    try {
      ok = org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
          cmdLine, new PrintWriter(so), new PrintWriter(se), null);
    } catch (Throwable t) {
      System.out.println("  THREW: " + t);
      return;
    }
    long t1 = System.currentTimeMillis();
    System.out.println("  success=" + ok + "  (" + (t1 - t0) + " ms)");
    if (!so.toString().isBlank()) System.out.println("  out: " + so.toString().trim());
    if (!se.toString().isBlank()) System.out.println("  err: " + se.toString().trim());
    File clazz = new File(out, cls + ".class");
    if (!ok || !clazz.exists()) {
      System.out.println("  -> FAILED");
      return;
    }
    System.out.println("  " + cls + ".class = " + clazz.length() + " bytes");
    try (URLClassLoader cl = new URLClassLoader(new URL[] {out.toURI().toURL()},
        EcjBatchProbe.class.getClassLoader())) {
      Method m = Class.forName(cls, true, cl).getMethod("main", String[].class);
      System.out.println("  ---- program output ----");
      m.invoke(null, (Object) new String[0]);
      System.out.println("  ------------------------");
    }
    System.out.println("  -> WORKS");
  }

  private static void line() {
    System.out.println("========================================================");
  }
}
