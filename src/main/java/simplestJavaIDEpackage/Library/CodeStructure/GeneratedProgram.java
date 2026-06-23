package simplestJavaIDEpackage.Library.CodeStructure;

import java.util.List;

/**
 * The set of Java files generated from a project (one per class), each with a map
 * of which generated line belongs to which editor tab. A span with
 * {@code methodIndex == -1} is the class header ("Klasse" tab); other spans are
 * method tabs. This lets a compiler error or debugger step be mapped back to the
 * exact class tab, method tab and local line.
 *
 * @author Daniel Trageser
 */
public record GeneratedProgram(List<GeneratedFile> files, String entryClassName) {

  /** A generated source file for one class. */
  public record GeneratedFile(int classIndex, String className, String code, List<MethodSpan> spans) {}

  /** A contiguous range of generated lines owned by one tab ({@code methodIndex == -1} = header). */
  public record MethodSpan(int methodIndex, String methodName, int startLine, int lineCount) {}

  /** Where a generated line maps to ({@code methodIndex == -1} = the class header tab). */
  public record MethodLocation(
      int classIndex, String className, int methodIndex, String methodName, int localLine) {}

  /** Maps a 1-based line in {@code className}'s generated file back to the editor. */
  public MethodLocation locate(String className, int generatedLine) {
    for (GeneratedFile file : files) {
      if (!file.className().equals(className)) {
        continue;
      }
      for (MethodSpan span : file.spans()) {
        if (generatedLine >= span.startLine()
            && generatedLine < span.startLine() + span.lineCount()) {
          return new MethodLocation(
              file.classIndex(),
              className,
              span.methodIndex(),
              span.methodName(),
              generatedLine - span.startLine() + 1);
        }
      }
      return null;
    }
    return null;
  }
}
