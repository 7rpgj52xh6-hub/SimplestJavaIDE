package simplestJavaIDEpackage.Library;

import java.io.Serializable;

public class Methods implements Serializable {

  private static final long serialVersionUID = 4518889829751756148L;
  private final String content;
  private final String name;

  public Methods(String name, String content) {
    this.content = content;
    this.name = name;
  }

  public String getContent() {
    return content;
  }

  public String getName() {
    return name;
  }
}
