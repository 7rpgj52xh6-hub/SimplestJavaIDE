package simplestJavaIDEpackage.Library.CodeStructure;

import java.util.ArrayList;

/**
 * One class in a project.
 *
 * <p>Methods are always edited as separate {@link #methods} tabs. In expert/class
 * mode the class additionally has a {@link #header} &mdash; the "Klasse" view
 * ({@code public class Name { ...attributes... }}) into which the method bodies
 * are injected when the program is generated. In the default mode the header is
 * unused and the wrapper class is generated automatically.
 *
 * @author Daniel Trageser
 */
public class JavaClass {

  public String name;
  public ArrayList<Methods> methods;
  /** Class shell with attributes/constructor (expert mode); empty otherwise. */
  public String header;

  public JavaClass(String name) {
    this.name = name;
    this.methods = new ArrayList<>();
    this.header = "";
  }

  /** True if this class is the program's entry point (contains a {@code main}). */
  public boolean hasMain() {
    if (header != null && header.contains("void main(")) {
      return true;
    }
    for (Methods method : methods) {
      if (method.content().contains("void main(")) {
        return true;
      }
    }
    return false;
  }
}
