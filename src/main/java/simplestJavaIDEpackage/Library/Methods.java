package simplestJavaIDEpackage.Library;

import java.io.Serializable;

public class Methods implements Serializable {

	private static final long serialVersionUID = 4518889829751756148L;
	private String contents = "";
	
	public Methods(String contents) {
		this.contents = contents;
	}
	
	public String getContent() {
		return this.contents;
	}
	
}
