package probe;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The viable Variant-A compile path: ECJ's BATCH compiler (which the spike proved
 * works under CheerpJ with {@code -source 1.8 -target 1.8 -bootclasspath rt8.jar})
 * PLUS {@code -log problems.xml}, which makes ECJ write a STRUCTURED problem log
 * (severity, line, id, message, per source file). We parse that XML back into the
 * data the app needs to map errors onto a method tab — so we keep structured
 * diagnostics without ever touching ECJ's getTask/JSR-199 path (which probes the
 * absent jrt image under CheerpJ).
 *
 * Proves: (1) batch compile of a broken class yields a parseable XML problem with
 * the right source + line, (2) a valid class compiles and runs in-JVM.
 *
 * Run: cheerpjRunMain("probe.EcjBatchXmlProbe", "/app/probe.jar:/app/ecj.jar")
 */
public class EcjBatchXmlProbe {

  public static void main(String[] args) throws Exception {
    line();
    System.out.println("ECJ batch + XML problem-log probe — java " + System.getProperty("java.version"));
    line();

    File work = new File("/files/batchxml-work");
    work.mkdirs();

    String broken =
        "public class Broken {\n"
      + "  public static void main(String[] a){\n"
      + "    int x = 5\n"                               // missing ';' on line 3
      + "    System.out.println(x);\n"
      + "  }\n}\n";
    String valid =
        "import java.util.ArrayList;\n"
      + "public class Hello8 {\n"
      + "  public static void main(String[] a){\n"
      + "    ArrayList<String> l = new ArrayList<>();\n"
      + "    l.add(\"Hallo vom Batch+XML-Pfad!\");\n"
      + "    for (String s : l) System.out.println(s);\n"
      + "  }\n}\n";

    System.out.println("\n>>> Broken: expect an ERROR problem, source=Broken.java, line=3");
    compileAndParse(work, "Broken", broken);

    System.out.println("\n>>> Hello8: expect success, no problems, then it runs");
    File out = compileAndParse(work, "Hello8", valid);
    if (out != null) runClass(out, "Hello8");

    line();
  }

  /** Compiles one class via ECJ batch + XML log, parses and prints the problems. */
  private static File compileAndParse(File work, String cls, String code) throws Exception {
    File dir = new File(work, cls);
    dir.mkdirs();
    File out = new File(dir, "out");
    out.mkdirs();
    File src = new File(dir, cls + ".java");
    File xml = new File(dir, "problems.xml");
    Files.write(src.toPath(), code.getBytes(StandardCharsets.UTF_8));

    String[] argv = {
      "-source", "1.8", "-target", "1.8",
      "-bootclasspath", "/app/rt8.jar",
      "-proc:none", "-nowarn",
      "-d", out.getAbsolutePath(),
      "-log", xml.getAbsolutePath(),
      src.getAbsolutePath()
    };

    StringWriter so = new StringWriter(), se = new StringWriter();
    long t0 = System.currentTimeMillis();
    boolean ok;
    try {
      ok = org.eclipse.jdt.core.compiler.batch.BatchCompiler.compile(
          argv, new PrintWriter(so), new PrintWriter(se), null);
    } catch (Throwable t) {
      System.out.println("  THREW: " + t);
      t.printStackTrace(System.out);
      return null;
    }
    long t1 = System.currentTimeMillis();
    System.out.println("  batch success=" + ok + "  (" + (t1 - t0) + " ms)");

    // Parse the XML problem log into the fields the app needs.
    if (!xml.exists()) {
      System.out.println("  (!) no problems.xml written");
    } else {
      System.out.println("  problems.xml = " + xml.length() + " bytes");
      parseProblems(xml);
    }
    File clazz = new File(out, cls + ".class");
    System.out.println("  " + cls + ".class = " + (clazz.exists() ? clazz.length() + " bytes" : "MISSING"));
    return (ok && clazz.exists()) ? out : null;
  }

  private static void parseProblems(File xml) {
    try {
      var dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
      Document doc = dbf.newDocumentBuilder().parse(xml);
      NodeList sources = doc.getElementsByTagName("source");
      System.out.println("  <source> elements: " + sources.getLength());
      NodeList problems = doc.getElementsByTagName("problem");
      System.out.println("  <problem> elements: " + problems.getLength());
      for (int i = 0; i < problems.getLength(); i++) {
        Element p = (Element) problems.item(i);
        String sourcePath = sourcePathOf(p);
        String message = "";
        NodeList msg = p.getElementsByTagName("message");
        if (msg.getLength() > 0) message = ((Element) msg.item(0)).getAttribute("value");
        System.out.println("    problem: severity=" + p.getAttribute("severity")
            + " id=" + p.getAttribute("id")
            + " problemID=" + p.getAttribute("problemID")
            + " line=" + p.getAttribute("line")
            + " source=" + basename(sourcePath)
            + " msg=\"" + message + "\"");
      }
    } catch (Throwable t) {
      System.out.println("  XML parse via DOM failed (" + t + ") — using regex fallback");
      try { regexParse(xml); } catch (Throwable t2) { System.out.println("  regex parse failed: " + t2); }
    }
  }

  /** Network-free parse of ECJ's problem log (the DTD reference breaks DOM under CheerpJ). */
  private static void regexParse(File xml) throws Exception {
    String s = Files.readString(xml.toPath());
    java.util.regex.Matcher m = java.util.regex.Pattern.compile(
        "<source\\s+path=\"([^\"]*)\"|<problem\\b([^>]*?)>", java.util.regex.Pattern.DOTALL).matcher(s);
    String curSource = "?";
    int count = 0;
    while (m.find()) {
      if (m.group(1) != null) {
        curSource = m.group(1);
      } else {
        String attrs = m.group(2);
        count++;
        System.out.println("    [regex] problem: severity=" + attr(attrs, "severity")
            + " id=" + attr(attrs, "id")
            + " problemID=" + attr(attrs, "problemID")
            + " line=" + attr(attrs, "line")
            + " source=" + basename(curSource));
      }
    }
    System.out.println("  [regex] total problems: " + count);
    java.util.regex.Matcher mm =
        java.util.regex.Pattern.compile("<message\\s+value=\"([^\"]*)\"").matcher(s);
    int i = 0;
    while (mm.find() && i < 3) { System.out.println("    [regex] message: " + mm.group(1)); i++; }
  }

  private static String attr(String attrs, String name) {
    java.util.regex.Matcher m = java.util.regex.Pattern.compile(name + "=\"([^\"]*)\"").matcher(attrs);
    return m.find() ? m.group(1) : "";
  }

  private static String sourcePathOf(Element problem) {
    org.w3c.dom.Node n = problem.getParentNode();      // <problems>
    while (n != null && !(n instanceof Element e && "source".equals(e.getTagName()))) {
      n = n.getParentNode();
    }
    return n == null ? "?" : ((Element) n).getAttribute("path");
  }

  private static String basename(String path) {
    int i = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
    return i >= 0 ? path.substring(i + 1) : path;
  }

  private static void runClass(File out, String cls) throws Exception {
    try (URLClassLoader cl = new URLClassLoader(new URL[] {out.toURI().toURL()}, EcjBatchXmlProbe.class.getClassLoader())) {
      Method m = Class.forName(cls, true, cl).getMethod("main", String[].class);
      System.out.println("  ---- program output ----");
      m.invoke(null, (Object) new String[0]);
      System.out.println("  ------------------------");
    }
  }

  private static void line() {
    System.out.println("========================================================");
  }
}
