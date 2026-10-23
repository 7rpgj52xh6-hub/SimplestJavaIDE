package probe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * THE decisive Variant-A probe: can we keep the IDE's structured-diagnostics path
 * under CheerpJ? We obtain ECJ via {@link ServiceLoader} (no compile-time ECJ dep,
 * exactly how the app will do it in web mode), drive it through
 * {@code compiler.getTask(...)} with a FULLY CUSTOM {@link JavaFileManager} that
 * serves platform classes from a bundled {@code rt8.jar} and never calls
 * {@code getStandardFileManager()} (which the spike proved opens the absent jrt
 * image under CheerpJ). We compile a broken and a valid class and check that the
 * {@link Diagnostic}s carry source name + line number (what maps errors to a tab),
 * then classload+run the valid one in THIS JVM.
 *
 * Run: cheerpjRunMain("probe.EcjTaskProbe", "/app/probe.jar:/app/ecj.jar")
 */
public class EcjTaskProbe {

  public static void main(String[] args) throws Exception {
    line();
    System.out.println("ECJ getTask + custom JavaFileManager probe — java " + System.getProperty("java.version"));
    line();

    JavaCompiler ecj = null;
    for (JavaCompiler c : ServiceLoader.load(JavaCompiler.class)) { ecj = c; break; }
    System.out.println("ServiceLoader JavaCompiler            = " + (ecj == null ? "null" : ecj.getClass().getName()));
    System.out.println("ToolProvider.getSystemJavaCompiler()  = " + ToolProvider.getSystemJavaCompiler());
    if (ecj == null) { System.out.println("RESULT: no ECJ via ServiceLoader — abort."); line(); return; }

    File outDir = new File("/files/task-work/out");
    outDir.mkdirs();
    File[] old = outDir.listFiles();
    if (old != null) for (File f : old) deleteRec(f);

    String valid =
        "import java.util.ArrayList;\n"
      + "public class Hello8 {\n"
      + "  public static void main(String[] a){\n"
      + "    ArrayList<String> l = new ArrayList<>();\n"
      + "    l.add(\"Hallo vom getTask-Pfad!\");\n"
      + "    for (String s : l) System.out.println(s);\n"
      + "  }\n}\n";
    String broken =
        "public class Broken {\n"
      + "  public static void main(String[] a){\n"
      + "    int x = 5\n"                               // <-- missing ';'  (line 3)
      + "    System.out.println(x);\n"
      + "  }\n}\n";

    try (VfsFileManager fm = new VfsFileManager(new JarFile("/app/rt8.jar"), outDir)) {
      System.out.println("\n>>> expect ERROR diagnostic with source=Broken.java, line=3:");
      compileOne(ecj, fm, "Broken", broken);
      System.out.println("\n>>> expect success + no diagnostics:");
      boolean ok = compileOne(ecj, fm, "Hello8", valid);
      if (ok) runClass(outDir, "Hello8");
      else System.out.println("  (valid class did not compile — custom FileManager path NOT viable)");
    }
    line();
    System.out.println("RESULT: if Broken reported source+line AND Hello8 compiled+ran, the");
    System.out.println("structured getTask path works under CheerpJ -> keep app's error mapping.");
    line();
  }

  private static boolean compileOne(JavaCompiler ecj, VfsFileManager fm, String cls, String code) {
    System.out.println("### compiling " + cls);
    DiagnosticCollector<JavaFileObject> diags = new DiagnosticCollector<>();
    JavaFileObject src = new StringSource(cls, code);
    // -bootclasspath is essential: without it ECJ's getTask path defaults the
    // platform to the running JDK's jrt module image (/lt/17), which CheerpJ has
    // no file for. Pointing it at the bundled rt8.jar is the proven Java-8 recipe.
    List<String> options = Arrays.asList("-source", "1.8", "-target", "1.8",
        "-bootclasspath", "/app/rt8.jar", "-proc:none", "-nowarn");
    long t0 = System.currentTimeMillis();
    Boolean ok;
    try {
      ok = ecj.getTask(null, fm, diags, options, null, Collections.singletonList(src)).call();
    } catch (Throwable t) {
      System.out.println("  THREW: " + t);
      t.printStackTrace(System.out);
      return false;
    }
    long t1 = System.currentTimeMillis();
    System.out.println("  success=" + ok + "  (" + (t1 - t0) + " ms)");
    for (Diagnostic<? extends JavaFileObject> d : diags.getDiagnostics()) {
      JavaFileObject s = d.getSource();
      System.out.println("  diag kind=" + d.getKind()
          + " source=" + (s == null ? "<null>" : s.getName())
          + " line=" + d.getLineNumber()
          + " col=" + d.getColumnNumber()
          + " code=" + d.getCode()
          + " msg=" + d.getMessage(null));
    }
    return Boolean.TRUE.equals(ok);
  }

  private static void runClass(File outDir, String cls) throws Exception {
    try (URLClassLoader cl = new URLClassLoader(new URL[] {outDir.toURI().toURL()}, EcjTaskProbe.class.getClassLoader())) {
      Method m = Class.forName(cls, true, cl).getMethod("main", String[].class);
      System.out.println("  ---- program output ----");
      m.invoke(null, (Object) new String[0]);
      System.out.println("  ------------------------");
    }
  }

  private static void deleteRec(File f) {
    File[] kids = f.listFiles();
    if (kids != null) for (File k : kids) deleteRec(k);
    f.delete();
  }

  private static void line() {
    System.out.println("========================================================");
  }

  // ---- a binary name carrier so inferBinaryName is exact -------------------
  private interface Named { String binaryName(); }

  // ---- the custom file manager (the thing we promote into the app) ---------
  static final class VfsFileManager implements JavaFileManager {
    private final JarFile platformJar; // rt8.jar — PLATFORM_CLASS_PATH
    private final File outDir;         // CLASS_OUTPUT (+ read back as CLASS_PATH)

    VfsFileManager(JarFile platformJar, File outDir) {
      this.platformJar = platformJar;
      this.outDir = outDir;
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) {
      List<JavaFileObject> out = new ArrayList<>();
      String pkgPath = packageName.replace('.', '/');
      if (kinds.contains(Kind.CLASS)
          && (location == StandardLocation.PLATFORM_CLASS_PATH || location == StandardLocation.CLASS_PATH)) {
        String prefix = pkgPath.isEmpty() ? "" : pkgPath + "/";
        Enumeration<JarEntry> en = platformJar.entries();
        while (en.hasMoreElements()) {
          JarEntry e = en.nextElement();
          String name = e.getName();
          if (!name.endsWith(".class") || !name.startsWith(prefix)) continue;
          String rest = name.substring(prefix.length());
          if (!recurse && rest.indexOf('/') >= 0) continue;
          String binary = name.substring(0, name.length() - 6).replace('/', '.');
          out.add(new JarClassObject(platformJar, e, binary));
        }
      }
      if (kinds.contains(Kind.CLASS)
          && (location == StandardLocation.CLASS_OUTPUT || location == StandardLocation.CLASS_PATH)) {
        File dir = pkgPath.isEmpty() ? outDir : new File(outDir, pkgPath);
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) {
          if (f.isFile() && f.getName().endsWith(".class")) {
            String base = f.getName().substring(0, f.getName().length() - 6);
            String binary = packageName.isEmpty() ? base : packageName + "." + base;
            out.add(new DirClassObject(f, binary));
          } else if (recurse && f.isDirectory()) {
            String sub = packageName.isEmpty() ? f.getName() : packageName + "." + f.getName();
            for (JavaFileObject o : list(location, sub, kinds, true)) out.add(o);
          }
        }
      }
      return out;
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
      if (file instanceof Named n) return n.binaryName();
      String n = file.getName();
      return n.endsWith(".class") ? n.substring(0, n.length() - 6).replace('/', '.') : n;
    }

    @Override public boolean isSameFile(FileObject a, FileObject b) { return a.toUri().equals(b.toUri()); }
    @Override public boolean handleOption(String current, Iterator<String> remaining) { return false; }

    @Override
    public boolean hasLocation(Location location) {
      return location == StandardLocation.PLATFORM_CLASS_PATH
          || location == StandardLocation.CLASS_PATH
          || location == StandardLocation.CLASS_OUTPUT
          || location == StandardLocation.SOURCE_PATH;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
      if (kind == Kind.CLASS) {
        String path = className.replace('.', '/') + ".class";
        File f = new File(outDir, path);
        if (f.exists()) return new DirClassObject(f, className);
        JarEntry e = platformJar.getJarEntry(path);
        if (e != null) return new JarClassObject(platformJar, e, className);
      }
      return null;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) {
      File f = new File(outDir, className.replace('.', '/') + kind.extension);
      File parent = f.getParentFile();
      if (parent != null) parent.mkdirs();
      return new OutputClassObject(f, className);
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) {
      File f = new File(packageName.isEmpty() ? outDir : new File(outDir, packageName.replace('.', '/')), relativeName);
      return f.exists() ? new DirClassObject(f, packageName + "/" + relativeName) : null;
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) {
      File f = new File(packageName.isEmpty() ? outDir : new File(outDir, packageName.replace('.', '/')), relativeName);
      File parent = f.getParentFile();
      if (parent != null) parent.mkdirs();
      return new OutputClassObject(f, packageName + "/" + relativeName);
    }

    @Override public ClassLoader getClassLoader(Location location) { return null; } // -proc:none
    @Override public void flush() {}
    @Override public void close() throws IOException { platformJar.close(); }
    @Override public int isSupportedOption(String option) { return -1; }
  }

  static final class JarClassObject extends SimpleJavaFileObject implements Named {
    private final JarFile jar;
    private final JarEntry entry;
    private final String binary;
    JarClassObject(JarFile jar, JarEntry entry, String binary) {
      super(URI.create("jar:///" + entry.getName()), Kind.CLASS);
      this.jar = jar; this.entry = entry; this.binary = binary;
    }
    @Override public String binaryName() { return binary; }
    @Override public InputStream openInputStream() throws IOException { return jar.getInputStream(entry); }
    @Override public String getName() { return entry.getName(); }
  }

  static final class DirClassObject extends SimpleJavaFileObject implements Named {
    private final File file;
    private final String binary;
    DirClassObject(File file, String binary) { super(file.toURI(), Kind.CLASS); this.file = file; this.binary = binary; }
    @Override public String binaryName() { return binary; }
    @Override public InputStream openInputStream() throws IOException { return new FileInputStream(file); }
  }

  static final class OutputClassObject extends SimpleJavaFileObject implements Named {
    private final File file;
    private final String binary;
    OutputClassObject(File file, String binary) { super(file.toURI(), Kind.CLASS); this.file = file; this.binary = binary; }
    @Override public String binaryName() { return binary; }
    @Override public OutputStream openOutputStream() throws IOException { return new FileOutputStream(file); }
  }

  static final class StringSource extends SimpleJavaFileObject {
    private final String code;
    private final String name;
    StringSource(String className, String code) {
      super(URI.create("string:///" + className + ".java"), Kind.SOURCE);
      this.code = code;
      this.name = className + ".java"; // basename — so the app's classNameOf() keeps working
    }
    @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) { return code; }
    @Override public String getName() { return name; }
  }
}
