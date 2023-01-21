package simplestJavaIDEpackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Daniel Trageser This class regulates loading, saving and formatting of the code
 *
 */
public class CodingFile {
  File savefile;
  String className;
  List<String> fullCode;
  public boolean isFinishedProcessing = false;
  public boolean isSaved = false;
  public InputStream is;

  public enum CodeMode {
    STANDARD, ADVANCED, EXPERT
  }

  CodingFile(File sf, Boolean isNewFile) {
    fullCode = new ArrayList<String>();
    fullCode.add(""); // Imports
    fullCode.add(""); // ClassStump
    fullCode.add("public static void main(String[] args){"); // MainMethodStump
    fullCode.add(""); // WrittenCode
    fullCode.add("\t}"); // End of Method(s)
    fullCode.add("}"); // End of Class
    getClassNameFromFile(sf.getName());
    if (!isNewFile) {
      loadCodeFromFile(sf);
      fullCode.set(1, "public class " + this.className + " {"); // Set class stump
      isFinishedProcessing = true;
    } else {
      fullCode.set(3, "System.out.println(\"Hello World\");");
      fullCode.set(1, "public class " + this.className + " {"); // Set class stump
      isFinishedProcessing = true;
    }
    savefile = sf;
  }

  private String trim(String str) {
    return str.replaceAll("(?m)^\\s+$", "").replaceAll("(?m)^\\n", "");
  }

  private String deleteMainFuctionCodeFromCode(String code) {
    if (code.contains("public static void main(String[] args){")) {
      return trim(code.replace("public static void main(String[] args){", "")
          .replaceFirst("(?s)(.*)" + "}", "$1" + ""));
    } else {
      return code;
    }
  }

  private String deleteClassFuctionCodeFromCode(String code) {
    if ((code.contains("public class")) && (code.contains("\n"))) {
      String toBeDeleted = code.substring(code.indexOf("public class"), code.indexOf("{\n") + 1);
      return trim(code.replace(toBeDeleted, "").replaceFirst("\n", "")
          .replaceFirst("(?s)(.*)" + "}", "$1" + ""));
    } else {
      return code;
    }
  }

  private String getStandardCode(String wholeCode) {
    return deleteMainFuctionCodeFromCode(deleteClassFuctionCodeFromCode(wholeCode));
  }

  public void getClassNameFromFile(String fileName) {
    String cn = fileName.replace(".java", "");
    className = cn;
  }

  public void saveToFile() {
    isSaved = true;
    String finalCode = getFileCode();
    try {
      FileOutputStream fos = new FileOutputStream(savefile);
      fos.write(finalCode.getBytes());
      fos.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getAbsolutePath() {
    return savefile.getAbsolutePath();
  }

  public String getClassPath() {
    return getAbsolutePath().replace(this.className, "").replace(".java", "");

  }

  public String getCode(CodeMode codeMode) {
    switch (codeMode) {
      case STANDARD:
        return this.getWrittenCode(0);
      case ADVANCED:
        return this.getExtendedCode();
      case EXPERT:
        return this.getFullCode();
      default:
        ErrorPopupWindow.throwMessage("Error with mode switch button. Mode was not set correcty.");
        return null;
    }
  }

  private void setWrittenCode(String input) {
    if (input.contains("\n")) {
      String[] inputLines = input.split("\n");
      String result = "";
      for (String i : inputLines) {
        i = i.replaceAll("\t", "");
        result = result + i + "\n";
      }
      fullCode.set(3, result);
    } else {
      fullCode.set(3, input.replaceAll("\t", ""));
    }
  }

  private String getWrittenCode(Integer amountOfTabs) {
    String source = fullCode.get(3).toString();
    List<String> output = new ArrayList<String>();
    if (source.contains("\n")) {
      String[] linesOfWrittenCode = source.split("\n");
      for (String s : linesOfWrittenCode) {
        for (int i = 0; i < amountOfTabs; i++) {
          s = "\t" + s;
        }
        output.add(s);
      }
      source = String.join("\n", output);
    } else {
      for (int i = 0; i < amountOfTabs; i++) {
        source = "\t" + source;
      }
    }
    return source;
  }

  private String getExtendedCode() {
    return trim(this.fullCode.get(2).toString() + "\n" + getWrittenCode(1) + "\n"
        + this.fullCode.get(4).toString().replaceFirst("(?s)(.*)" + "\\t", "$1" + ""));
  }

  private String getFullCode() {
    return trim(
        fullCode.get(1).toString() + "\n\t" + fullCode.get(2).toString() + "\n" + getWrittenCode(2)
            + "\n" + fullCode.get(4).toString() + "\n" + fullCode.get(5).toString());
  }

  private String getFileCode() {
    if (this.fullCode.get(0).toString().contains("import")) { // different if imports exist
                                                              // (additional new line)
      return trim(fullCode.get(0).toString() + "\n" + getFullCode());
    } else {
      return trim(getFullCode());
    }
  }

  public void setImports(String i) {
    this.fullCode.set(0, i);
  }

  public String getImports() {
    return this.fullCode.get(0).toString();
  }

  public void loadCodeFromFile(File file) {
    String sCurrentLine;
    StringBuilder contentBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
      while ((sCurrentLine = br.readLine()) != null) {
        contentBuilder.append(sCurrentLine).append("\n");
      }
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }

    String wholeCode = contentBuilder.toString();

    String[] wholeCodeInLines = wholeCode.split("\n");
    String allImports = "";
    for (String i : wholeCodeInLines) {
      // Get all imports
      if (i.matches("(?s).*\\bimport\\s*(\\S+);$")) {
        i.replaceAll("\n", "");
        i = i + "\n";
        allImports = allImports + i;
      }
    }
    fullCode.set(0, allImports); // set import part

    if (this.className != null) {
      fullCode.set(1, "public class " + this.className + " {"); // set class stump
    }
    setWrittenCode(trim(getStandardCode(wholeCode.replace(allImports, ""))));
  }

  public String getClassName() {
    return this.className;
  }

  public void writeAllCodeToArray(String text, CodeMode codeMode) {
    switch (codeMode) {
      case ADVANCED:
        setWrittenCode(getStandardCode(text));
        break;
      case STANDARD:
        setWrittenCode(text);
        break;
      case EXPERT:
        setWrittenCode(getStandardCode(text));
        break;
      default:
        ErrorPopupWindow.throwMessage("Error with mode switch button. Mode was not set correcty.");
        break;
    }
  }
}
