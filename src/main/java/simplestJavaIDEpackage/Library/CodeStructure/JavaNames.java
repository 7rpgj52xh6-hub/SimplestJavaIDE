package simplestJavaIDEpackage.Library.CodeStructure;

import java.util.Set;

/**
 * Validation for Java identifiers (class and method names).
 *
 * @author Daniel Trageser
 */
public final class JavaNames {

  private static final Set<String> RESERVED =
      Set.of(
          "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
          "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
          "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
          "interface", "long", "native", "new", "package", "private", "protected", "public",
          "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
          "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false",
          "null");

  private JavaNames() {}

  /** True if {@code name} is a valid, non-reserved Java identifier. */
  public static boolean isValidIdentifier(String name) {
    if (name == null || name.isEmpty() || RESERVED.contains(name)) {
      return false;
    }
    if (!Character.isJavaIdentifierStart(name.charAt(0))) {
      return false;
    }
    for (int i = 1; i < name.length(); i++) {
      if (!Character.isJavaIdentifierPart(name.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /** A short reason why {@code name} is not a valid identifier, or null if it is valid. */
  public static String invalidReason(String name) {
    if (name == null || name.isEmpty()) {
      return "Der Name darf nicht leer sein.";
    }
    if (RESERVED.contains(name)) {
      return "'" + name + "' ist ein reserviertes Java-Schlüsselwort.";
    }
    if (!Character.isJavaIdentifierStart(name.charAt(0))) {
      return "Der Name muss mit einem Buchstaben oder _ beginnen (keine Ziffer).";
    }
    for (int i = 1; i < name.length(); i++) {
      if (!Character.isJavaIdentifierPart(name.charAt(i))) {
        return "Der Name darf nur Buchstaben, Ziffern und _ enthalten (kein '" + name.charAt(i) + "').";
      }
    }
    return null;
  }
}
