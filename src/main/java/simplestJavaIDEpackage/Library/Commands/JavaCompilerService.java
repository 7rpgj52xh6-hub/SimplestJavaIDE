package simplestJavaIDEpackage.Library.Commands;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import simplestJavaIDEpackage.Platform;

/**
 * Compiles the generated Java file(s) in-process and returns structured
 * {@link Diagnostic}s (with line numbers), which is what lets the IDE map an
 * error back to a method tab.
 *
 * <p>Two back-ends, picked by {@link Platform}:
 *
 * <ul>
 *   <li><b>Desktop</b> — the JDK system compiler ({@code javax.tools}). Needs a
 *       JDK (not a bare JRE) and keeps {@code -g} so the step debugger can read
 *       local variables.
 *   <li><b>Web (CheerpJ)</b> — the Eclipse compiler (ECJ), reached by reflection
 *       so the Desktop build has no compile-time ECJ dependency. CheerpJ ships no
 *       {@code javax.tools} system compiler, and ECJ's JSR-199 ({@code getTask})
 *       path probes the absent jrt module image, so we drive ECJ's BATCH compiler
 *       with a bundled Java-8 bootclasspath and {@code -log problems.xml}, then
 *       parse that log back into {@link Diagnostic}s (DOM parsing fails under
 *       CheerpJ because of the log's DTD, so the parse is regex-based).
 * </ul>
 *
 * @author Daniel Trageser
 */
public class JavaCompilerService {

  /**
   * @param compilerAvailable false when running on a JRE (no compiler present)
   * @param success true when compilation produced no errors
   * @param diagnostics all messages the compiler emitted
   */
  public record Result(
      boolean compilerAvailable,
      boolean success,
      List<Diagnostic<? extends JavaFileObject>> diagnostics) {}

  public static Result compile(List<String> javaFilePaths, String outputDir) {
    return Platform.WEB
        ? compileWithEcj(javaFilePaths, outputDir)
        : compileWithSystemCompiler(javaFilePaths, outputDir);
  }

  // ---- Desktop: the JDK system compiler (unchanged behaviour) ---------------

  private static Result compileWithSystemCompiler(List<String> javaFilePaths, String outputDir) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      return new Result(false, false, List.of());
    }
    List<File> sourceFiles = new ArrayList<>();
    for (String path : javaFilePaths) {
      sourceFiles.add(new File(path));
    }
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
      Iterable<? extends JavaFileObject> units =
          fileManager.getJavaFileObjectsFromFiles(sourceFiles);
      // -g keeps local variable tables so the step debugger can read variables.
      List<String> options = List.of("-d", outputDir, "-g");
      boolean success =
          compiler.getTask(null, fileManager, diagnostics, options, null, units).call();
      return new Result(true, success, diagnostics.getDiagnostics());
    } catch (Exception e) {
      return new Result(true, false, diagnostics.getDiagnostics());
    }
  }

  // ---- Web: ECJ batch + XML problem log (CheerpJ-safe) ----------------------

  /** Bundled Java-8 platform jar used as ECJ's bootclasspath (overridable). */
  private static final String RT8 = System.getProperty("sji.rt8", "/app/rt8.jar");

  private static Result compileWithEcj(List<String> javaFilePaths, String outputDir) {
    File log = new File(outputDir, "ecj-problems.xml");
    log.delete();
    List<String> argv = new ArrayList<>(List.of(
        "-source", "1.8", "-target", "1.8",
        "-bootclasspath", RT8,
        "-proc:none", "-nowarn",
        "-d", outputDir,
        "-log", log.getAbsolutePath()));
    argv.addAll(javaFilePaths);

    boolean ok;
    try {
      Class<?> batch = Class.forName("org.eclipse.jdt.core.compiler.batch.BatchCompiler");
      Class<?> progress = Class.forName("org.eclipse.jdt.core.compiler.CompilationProgress");
      Method compile =
          batch.getMethod("compile", String[].class, PrintWriter.class, PrintWriter.class, progress);
      Object result =
          compile.invoke(
              null,
              (Object) argv.toArray(new String[0]),
              new PrintWriter(new StringWriter()),
              new PrintWriter(new StringWriter()),
              null);
      ok = Boolean.TRUE.equals(result);
    } catch (Throwable t) {
      String first = javaFilePaths.isEmpty() ? null : javaFilePaths.get(0);
      return new Result(
          true,
          false,
          List.of(new XmlDiagnostic(Diagnostic.Kind.ERROR, new SourceRef(first), 0, "ecj.invoke",
              "Interner Compiler-Fehler (ECJ): " + t)));
    }

    List<Diagnostic<? extends JavaFileObject>> diagnostics = parseProblemLog(log);
    boolean hasError =
        diagnostics.stream().anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR);
    return new Result(true, ok && !hasError, diagnostics);
  }

  private static final Pattern ENTRY =
      Pattern.compile("<source\\b[^>]*\\bpath=\"([^\"]*)\"|<problem\\b([^>]*)>(.*?)</problem>",
          Pattern.DOTALL);
  private static final Pattern MESSAGE =
      Pattern.compile("<message\\b[^>]*\\bvalue=\"([^\"]*)\"", Pattern.DOTALL);

  /** Network-free parse of ECJ's XML problem log (its DTD breaks DOM under CheerpJ). */
  private static List<Diagnostic<? extends JavaFileObject>> parseProblemLog(File log) {
    List<Diagnostic<? extends JavaFileObject>> out = new ArrayList<>();
    if (!log.isFile()) {
      return out;
    }
    String xml;
    try {
      xml = Files.readString(log.toPath());
    } catch (Exception e) {
      return out;
    }
    Matcher m = ENTRY.matcher(xml);
    String currentSource = null;
    while (m.find()) {
      if (m.group(1) != null) {
        currentSource = m.group(1);
        continue;
      }
      String attrs = m.group(2);
      String severity = attr(attrs, "severity");
      if (severity.isEmpty()) {
        continue;
      }
      Matcher msg = MESSAGE.matcher(m.group(3));
      String message = msg.find() ? unescape(msg.group(1)) : "";
      out.add(
          new XmlDiagnostic(
              kindOf(severity),
              new SourceRef(currentSource),
              parseLong(attr(attrs, "line")),
              attr(attrs, "id"), // ECJ problem id, e.g. "ParsingErrorInsertToComplete"
              message));
    }
    return out;
  }

  private static String attr(String attrs, String name) {
    Matcher m = Pattern.compile(name + "=\"([^\"]*)\"").matcher(attrs);
    return m.find() ? m.group(1) : "";
  }

  private static long parseLong(String s) {
    try {
      return Long.parseLong(s.trim());
    } catch (RuntimeException e) {
      return 0;
    }
  }

  private static Diagnostic.Kind kindOf(String severity) {
    return switch (severity.toUpperCase(Locale.ROOT)) {
      case "ERROR", "FATAL" -> Diagnostic.Kind.ERROR;
      case "WARNING" -> Diagnostic.Kind.WARNING;
      default -> Diagnostic.Kind.NOTE;
    };
  }

  private static String unescape(String s) {
    return s.replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&#39;", "'")
        .replace("&amp;", "&");
  }

  /** A source reference whose {@code getName()} is the .java path, so classNameOf() works. */
  private static final class SourceRef extends SimpleJavaFileObject {
    private SourceRef(String path) {
      super(toUri(path), Kind.SOURCE);
    }

    private static URI toUri(String path) {
      try {
        return new File(path == null ? "Unknown.java" : path).toURI();
      } catch (RuntimeException e) {
        return URI.create("string:///Unknown.java");
      }
    }
  }

  /** A {@link Diagnostic} built from one ECJ XML {@code <problem>}. */
  private static final class XmlDiagnostic implements Diagnostic<JavaFileObject> {
    private final Kind kind;
    private final JavaFileObject source;
    private final long line;
    private final String code;
    private final String message;

    private XmlDiagnostic(Kind kind, JavaFileObject source, long line, String code, String message) {
      this.kind = kind;
      this.source = source;
      this.line = line;
      this.code = code;
      this.message = message;
    }

    @Override public Kind getKind() { return kind; }
    @Override public JavaFileObject getSource() { return source; }
    @Override public long getPosition() { return NOPOS; }
    @Override public long getStartPosition() { return NOPOS; }
    @Override public long getEndPosition() { return NOPOS; }
    @Override public long getLineNumber() { return line; }
    @Override public long getColumnNumber() { return NOPOS; }
    @Override public String getCode() { return code; }
    @Override public String getMessage(Locale locale) { return message; }
  }
}
