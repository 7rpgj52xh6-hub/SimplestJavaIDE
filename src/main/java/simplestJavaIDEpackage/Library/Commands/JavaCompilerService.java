package simplestJavaIDEpackage.Library.Commands;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Compiles the generated Java file in-process via the JDK compiler API. Compared
 * to shelling out to {@code javac}, this needs no {@code javac} on the PATH and
 * returns structured {@link Diagnostic}s (with line numbers) instead of text we
 * would have to parse — which is what makes mapping errors back to a method tab
 * possible.
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

  public static Result compile(String javaFilePath, String outputDir) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      return new Result(false, false, List.of());
    }
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
      Iterable<? extends JavaFileObject> units =
          fileManager.getJavaFileObjectsFromFiles(List.of(new File(javaFilePath)));
      // -g keeps local variable tables so the step debugger can read variables.
      List<String> options = List.of("-d", outputDir, "-g");
      boolean success =
          compiler.getTask(null, fileManager, diagnostics, options, null, units).call();
      return new Result(true, success, diagnostics.getDiagnostics());
    } catch (Exception e) {
      return new Result(true, false, diagnostics.getDiagnostics());
    }
  }
}
