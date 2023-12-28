package simplestJavaIDEpackage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

import simplestJavaIDEpackage.Library.Classes;
import simplestJavaIDEpackage.Library.Methods;

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
	public ArrayList<Methods> methods;
	public Classes _class;


	public CodingFile(String filepath) {
		this.filepath = filepath;
		this.imports = new ArrayList<>();
		this.methods = new ArrayList<>();
		this.methods.add(new Methods("Main Method","public static void main(String[] args){\n\tSystem.out.println(\"Hello World\");\n}"));
		this._class = new Classes(this.generateClassName());
	}

	public String generateFullImportsCode() {
		String result = "";
		for(String i: imports) {
			result = result + "import " + i + ";\n";
		}
		return result;
	}

	public String generateFullClassCode() {
		String result = generateFullImportsCode() + _class.getClassHead();
		// Get code of all methods
		result = result + generateCodeOfMethods();
		// Add footer
		result = result + "\n" + _class.getClassFooter();
		return result;
	}

	public String generateCodeOfMethods() {
		String result = "";
		for (Methods i : this.methods) {
			result = result + i.getContent() + "\n";
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
		return this.filepath.replace(this._class.getClassName(), "").replace(".sji", "");

	}


	public String generateFullJavaCode() {
		String result = generateFullClassCode();
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
		methods.set(methodIndex, new Methods("MethodennameXY",code));
	}

	public void saveImportCode(int importIndex, String code) {
		imports.set(importIndex, code);
	}

	public String getJavaTmpClassPath() {
		return getFilepath().replace(".sji", ".class");
	}

	public Boolean checkIfMethodWithSameNameExists(String name) {
		for (Methods i:methods) {
			if (i.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public Methods returnMethodFromName(String name) {
		for (Methods i:methods) {
			if (i.getName().equals(name)) {
				return i;
			}
		}
		return null;
	}

}
