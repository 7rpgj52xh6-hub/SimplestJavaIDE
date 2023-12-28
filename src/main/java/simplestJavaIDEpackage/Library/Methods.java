package simplestJavaIDEpackage.Library;

import java.io.Serializable;

public class Methods implements Serializable {

	private static final long serialVersionUID = 4518889829751756148L;
	private String head;
	private String body;
	private String footer = "}";

	public Methods(String head, String body) {
		this.head = head;
		this.body = body;
	}

	public String getContent() {
		return this.head + "\n" +  this.body + "\n" + this.footer;
	}

}
