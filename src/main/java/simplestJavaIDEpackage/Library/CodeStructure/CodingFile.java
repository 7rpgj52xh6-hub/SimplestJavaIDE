package simplestJavaIDEpackage.Library.CodeStructure;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.CodeStructure.GeneratedSource.MethodSpan;

/**
 * @author Daniel Trageser This class regulates loading, saving and formatting of the code
 */
public class CodingFile {
  public ArrayList<String> imports;
  public ArrayList<Methods> methods;
  public Classes javaClass;

  // The location of the file itself is not part of the saved content; it is set
  // from the actual path when the file is loaded (so moving a file keeps working).
  private transient String filepath;

  public CodingFile(String className, String filepath) {
    this.filepath = filepath;
    this.imports = new ArrayList<>();
    this.methods = new ArrayList<>();
    this.methods.add(
        new Methods(
            "Main Method",
            "public static void main(String[] args){\n\tSystem.out.println(\"Hello World\");\n}"));
    this.javaClass = new Classes(className);
  }

  public String generateFullImportsCode() {
    StringBuilder result = new StringBuilder();
    for (String i : imports) {
      result.append("import ").append(i).append(";\n");
    }
    return result.toString();
  }

  /**
   * Builds the full Java source that is compiled, recording which generated line
   * range belongs to which method so compiler errors can be mapped back to a tab.
   */
  public GeneratedSource buildSource() {
    StringBuilder code = new StringBuilder();
    List<MethodSpan> spans = new ArrayList<>();

    code.append(generateFullImportsCode());
    code.append(javaClass.classHead());
    // The next appended character starts on this 1-based line.
    int lineCursor = imports.size() + 1 /* class head */ + 1;

    for (int i = 0; i < methods.size(); i++) {
      String content = methods.get(i).content();
      int newlines = (int) content.chars().filter(ch -> ch == '\n').count();
      spans.add(new MethodSpan(i, methods.get(i).name(), lineCursor, newlines + 1));
      code.append(content).append("\n");
      lineCursor += newlines + 1;
    }

    code.append("\n").append(javaClass.classFooter());
    return new GeneratedSource(code.toString(), spans);
  }

  public String generateFullJavaCode() {
    return buildSource().code();
  }

  public String getFilepath() {
    return this.filepath;
  }

  public void setFilepath(String filepath) {
    this.filepath = filepath;
  }

  public String generateClassPath() {
    // The compiled .class lands next to the .sji/.java file, so the classpath is
    // simply that directory. String.replace() would mangle the path whenever the
    // class name also appears in a parent folder name.
    return new File(this.filepath).getParent();
  }

  public void tmpSaveAndRunJavaCode() {
    try {
      Files.writeString(
          new File(getJavaTmpFilePath()).toPath(), generateFullJavaCode(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
  }

  public String getJavaTmpFilePath() {
    return getFilepath().replace(".sji", ".java");
  }

  public String getJavaTmpClassPath() {
    return getFilepath().replace(".sji", ".class");
  }

  public Boolean checkIfMethodWithSameNameExists(String name) {
    for (Methods i : methods) {
      if (i.name().equals(name)) {
        return true;
      }
    }
    return false;
  }

  public Methods returnMethodFromName(String name) {
    for (Methods i : methods) {
      if (i.name().equals(name)) {
        return i;
      }
    }
    return null;
  }
}
