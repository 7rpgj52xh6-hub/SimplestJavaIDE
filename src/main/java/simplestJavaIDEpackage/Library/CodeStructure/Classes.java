package simplestJavaIDEpackage.Library.CodeStructure;

/**
 * The single generated wrapper class that hosts all of the user's methods. The
 * header and footer are derived from the class name.
 *
 * @author Daniel Trageser
 */
public record Classes(String className) {

  public String classHead() {
    return "public class " + className + " {\n";
  }

  public String classFooter() {
    return "}";
  }
}
