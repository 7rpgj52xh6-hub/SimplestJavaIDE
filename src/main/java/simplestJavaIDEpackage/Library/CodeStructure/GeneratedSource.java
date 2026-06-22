package simplestJavaIDEpackage.Library.CodeStructure;

import java.util.List;

/**
 * The full Java source that gets compiled, together with a map of which line
 * range in that generated file belongs to which user method. This is what lets
 * us translate a compiler error (reported against the generated file) back to a
 * method tab and a line the student actually sees.
 *
 * @author Daniel Trageser
 */
public record GeneratedSource(String code, List<MethodSpan> spans) {

  /** A contiguous range of generated lines owned by one user method. */
  public record MethodSpan(int methodIndex, String methodName, int startLine, int lineCount) {}

  /** Where a generated line maps to inside a user method. */
  public record MethodLocation(int methodIndex, String methodName, int localLine) {}

  /**
   * Maps a 1-based line in the generated file to a method and a local 1-based
   * line, or {@code null} if the line belongs to the imports, class head or
   * footer (i.e. code the student never sees).
   */
  public MethodLocation locate(int generatedLine) {
    for (MethodSpan span : spans) {
      if (generatedLine >= span.startLine()
          && generatedLine < span.startLine() + span.lineCount()) {
        return new MethodLocation(
            span.methodIndex(), span.methodName(), generatedLine - span.startLine() + 1);
      }
    }
    return null;
  }
}
