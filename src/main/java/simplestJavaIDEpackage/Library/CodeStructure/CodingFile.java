package simplestJavaIDEpackage.Library.CodeStructure;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedProgram.GeneratedFile;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedProgram.MethodSpan;

/**
 * A project: shared imports, one or more {@link JavaClass}es and an expert-mode
 * flag. Generates one Java file per class by taking the class shell (an
 * auto-generated wrapper in the default mode, or the student's {@code header} in
 * expert mode) and injecting the method bodies before its closing brace.
 *
 * @author Daniel Trageser
 */
public class CodingFile {

  private static final Pattern CLASS_DECL =
      Pattern.compile("(?:public\\s+)?(?:final\\s+|abstract\\s+)?class\\s+(\\w+)");

  public ArrayList<String> imports;
  public ArrayList<JavaClass> classes;
  public boolean expertMode;

  private transient String filepath;

  public CodingFile(String className, String filepath) {
    this.filepath = filepath;
    this.imports = new ArrayList<>();
    this.expertMode = false;
    this.classes = new ArrayList<>();
    JavaClass main = new JavaClass(className);
    main.methods.add(
        new Methods(
            "Main Method",
            "public static void main(String[] args){\n\tSystem.out.println(\"Hello World\");\n}"));
    this.classes.add(main);
  }

  public String generateFullImportsCode() {
    StringBuilder result = new StringBuilder();
    for (String i : imports) {
      result.append("import ").append(i).append(";\n");
    }
    return result.toString();
  }

  /** The class shell ("Klasse" view) for a class: its header or a default wrapper. */
  public String classShell(JavaClass javaClass) {
    if (expertMode && javaClass.header != null && !javaClass.header.isBlank()) {
      return javaClass.header;
    }
    return "public class " + javaClass.name + " {\n}";
  }

  /** The generated class name (parsed from the shell in expert mode, the tab name otherwise). */
  public String generatedClassName(JavaClass javaClass) {
    if (expertMode) {
      Matcher matcher = CLASS_DECL.matcher(classShell(javaClass));
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return javaClass.name;
  }

  /** The class run as the program: the project's main class if it has a main, else the first with one. */
  public JavaClass entryClass() {
    if (classes.get(0).hasMain()) {
      return classes.get(0);
    }
    for (JavaClass javaClass : classes) {
      if (javaClass.hasMain()) {
        return javaClass;
      }
    }
    return classes.get(0);
  }

  public int mainCount() {
    int count = 0;
    for (JavaClass javaClass : classes) {
      if (javaClass.hasMain()) {
        count++;
      }
    }
    return count;
  }

  /** The class name declared in a class shell ({@code public class X}), or null. */
  public static String declaredClassName(String source) {
    if (source == null) {
      return null;
    }
    Matcher matcher = CLASS_DECL.matcher(source);
    return matcher.find() ? matcher.group(1) : null;
  }

  /** The method name a method body declares (the identifier before its first {@code (}), or null. */
  public static String declaredMethodName(String content) {
    if (content == null) {
      return null;
    }
    int paren = content.indexOf('(');
    if (paren < 0) {
      return null;
    }
    int end = paren;
    while (end > 0 && Character.isWhitespace(content.charAt(end - 1))) {
      end--;
    }
    int start = end;
    while (start > 0 && Character.isJavaIdentifierPart(content.charAt(start - 1))) {
      start--;
    }
    String name = content.substring(start, end);
    return name.isEmpty() ? null : name;
  }

  public String entryClassName() {
    return generatedClassName(entryClass());
  }

  /** Builds one Java file per class, injecting the methods into each class shell. */
  public GeneratedProgram buildProgram() {
    String importsCode = generateFullImportsCode();
    int importsLines = imports.size();
    List<GeneratedFile> files = new ArrayList<>();

    for (int classIndex = 0; classIndex < classes.size(); classIndex++) {
      JavaClass javaClass = classes.get(classIndex);
      String shell = classShell(javaClass);
      int brace = shell.lastIndexOf('}');
      String pre = brace >= 0 ? shell.substring(0, brace) : shell;
      if (!pre.endsWith("\n")) {
        pre = pre + "\n";
      }

      StringBuilder code = new StringBuilder(importsCode).append(pre);
      List<MethodSpan> spans = new ArrayList<>();
      // Header span (the "Klasse" tab) maps to method index -1.
      spans.add(new MethodSpan(-1, javaClass.name, importsLines + 1, countNewlines(pre)));

      int lineCursor = importsLines + countNewlines(pre) + 1;
      for (int methodIndex = 0; methodIndex < javaClass.methods.size(); methodIndex++) {
        Methods method = javaClass.methods.get(methodIndex);
        int newlines = countNewlines(method.content());
        spans.add(new MethodSpan(methodIndex, method.name(), lineCursor, newlines + 1));
        code.append(method.content()).append("\n");
        lineCursor += newlines + 1;
      }
      code.append("}");
      files.add(new GeneratedFile(classIndex, generatedClassName(javaClass), code.toString(), spans));
    }
    return new GeneratedProgram(files, entryClassName());
  }

  private static int countNewlines(String text) {
    return (int) text.chars().filter(c -> c == '\n').count();
  }

  public String getFilepath() {
    return this.filepath;
  }

  public void setFilepath(String filepath) {
    this.filepath = filepath;
  }

  public String generateClassPath() {
    return new File(this.filepath).getParent();
  }

  /** Writes every class to its own {@code <ClassName>.java} in the project directory. */
  public void writeSources() {
    String dir = generateClassPath();
    for (GeneratedFile file : buildProgram().files()) {
      try {
        Files.writeString(
            Path.of(dir, file.className() + ".java"), file.code(), StandardCharsets.UTF_8);
      } catch (IOException e) {
        ErrorPopupWindow.throwMessage(e.getMessage());
      }
    }
  }

  /** The {@code .java} paths to compile (one per class). */
  public List<String> javaFilePaths() {
    String dir = generateClassPath();
    List<String> paths = new ArrayList<>();
    for (JavaClass javaClass : classes) {
      paths.add(new File(dir, generatedClassName(javaClass) + ".java").getAbsolutePath());
    }
    return paths;
  }

  /** All generated {@code .java}/{@code .class} files, for cleanup on close. */
  public List<String> generatedFilePaths() {
    String dir = generateClassPath();
    List<String> paths = new ArrayList<>();
    for (JavaClass javaClass : classes) {
      String name = generatedClassName(javaClass);
      paths.add(new File(dir, name + ".java").getAbsolutePath());
      paths.add(new File(dir, name + ".class").getAbsolutePath());
    }
    return paths;
  }
}
