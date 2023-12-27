package simplestJavaIDEpackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author Daniel Trageser This class regulates loading, saving and formatting
 *         of the code
 *
 */
public class CodingFile implements Serializable {
	private static final long serialVersionUID = -5935126133647208562L;
	String filepath;
	public ArrayList<String> imports;
	public ArrayList<String> methods;
	public String className;
	public String classHead;
	public String classFooter;

	public CodingFile(String filepath) {
		this.filepath = filepath;
		this.imports = new ArrayList<>();
		this.methods = new ArrayList<>();
		this.methods.add("public static void main(String[] args){\n\tSystem.out.println(\"Hello World\");\n}");
		this.className = this.generateClassName();
		this.classHead = "public class " + this.className + " {\n";
		this.classFooter = "}";
	}

	public String generateFullClassCode() {
		String result = classHead;
		// Get code of all methods
		for (String i : methods) {
			result = result + i + "\n";
		}
		// Add footer
		result = result + "\n" + classFooter;
		return result;
	}

	public String generateCodeOfMethods() {
		String result = "";
		for (String i : this.methods) {
			result = result + i + "\n\n";
		}
		return result;
	}

	public String getFilepath() {
		return this.filepath;
	}

	public String generateClassName() {
		String[] pathContents = this.filepath.split(Pattern.quote("/"));
		String result = pathContents[pathContents.length - 1].replace(".sji", "");
		return result;
	}

	public String generateClassPath() {
		return this.filepath.replace(this.className, "").replace(".sji", "");

	}

	public String getImports() {
		String result = "";
		for (String i : this.imports) {
			result = result + i + "\n\n";
		}
		return result;
	}

	public String generateFullJavaCode() {
		String result = getImports() + generateFullClassCode();
		return result;
	}

	public void tmpSaveAndRunJavaCode() {
		File output = new File(getJavaTmpFilePath());
		if (!output.exists()) {
			try {
				output.createNewFile();
			} catch (IOException e) {
				ErrorPopupWindow.throwMessage(e.getMessage());
			}
		}
		try {
			FileOutputStream fos = new FileOutputStream(getJavaTmpFilePath());
			byte[] outputInBytes = generateFullJavaCode().getBytes();
			try {
				fos.write(outputInBytes);
			} catch (IOException e) {
				ErrorPopupWindow.throwMessage(e.getMessage());
			}
		} catch (FileNotFoundException e) {
			ErrorPopupWindow.throwMessage(e.getMessage());
		}

	}

	public String getJavaTmpFilePath() {
		return getFilepath().replace(".sji", ".java");
	}

	public void saveMethodCode(int methodIndex, String code) {
		methods.set(methodIndex, code);
	}

	public void saveImportCode(int importIndex, String code) {
		imports.set(importIndex, code);
	}

	public String getJavaTmpClassPath() {
		return getFilepath().replace(".sji", ".class");
	}

}
