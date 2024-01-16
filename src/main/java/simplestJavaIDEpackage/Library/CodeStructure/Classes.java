package simplestJavaIDEpackage.Library.CodeStructure;

import java.io.Serializable;

public class Classes implements Serializable {
  private static final long serialVersionUID = -113166198969108150L;
  private final String className;
  private final String classHead;
  private final String classFooter;

  public Classes(String className) {
    this.className = className;
    this.classHead = "public class " + this.className + " {\n";
    this.classFooter = "}";
  }

  public String getClassName() {
    return className;
  }

  public String getClassHead() {
    return classHead;
  }

  public String getClassFooter() {
    return classFooter;
  }
}
