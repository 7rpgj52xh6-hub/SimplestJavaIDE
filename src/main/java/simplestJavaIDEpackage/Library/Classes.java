package simplestJavaIDEpackage.Library;

import java.io.Serializable;

public class Classes implements Serializable{
	private static final long serialVersionUID = -113166198969108150L;
	private String className;
	private String classHead;
	private String classFooter;

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
