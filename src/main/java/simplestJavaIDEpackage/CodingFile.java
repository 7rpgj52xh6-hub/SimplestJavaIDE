package simplestJavaIDEpackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CodingFile {
	File savefile;
	String className;
	List<String> fullCode;
	public boolean isFinishedProcessing = false;
	public boolean isSaved = false;
	public InputStream is;

	public enum CodeMode {
		STANDARD, EXTENDED
	}

	CodingFile(File sf, Boolean isNewFile) {
		fullCode = new ArrayList<String>();
		fullCode.add(""); // Imports
		fullCode.add(""); // ClassStump
		fullCode.add("public static void main(String[] args){"); // MainMethodStump
		fullCode.add(""); // WrittenCode
		fullCode.add("}"); // End of Method(s)
		fullCode.add("\n}"); // End of Class
		if (!isNewFile) {
			splitUpFileToItsContents(sf);
			getClassNameFromWholeCode();
			loadCodeFromWholeCode();
			isFinishedProcessing = true;
		} else {
			getClassNameFromFile(sf.getName());
			fullCode.set(3, "System.out.println(\"Hello World\");");
			fullCode.set(1, "public class " + this.className + " {"); // Set class stump
			isFinishedProcessing = true;
		}
		savefile = sf;
	}

	private void splitUpFileToItsContents(File file) {
		String sCurrentLine;
		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
			while ((sCurrentLine = br.readLine()) != null) {
				contentBuilder.append(sCurrentLine).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		String wholeCode = contentBuilder.toString();
		// TODO Split everything up

		String[] wholeCodeInLines = wholeCode.split("\n");
		String allImports = "";
		String writtenCode = "";
		for (String i : wholeCodeInLines) {
			// Get all imports
			if (i.matches("(?s).*\\bimport\\s*(\\S+);$")) {
				i.replaceAll("\n", "");
				i = i + "\n";
				allImports = allImports + i;
			}
		}
		fullCode.set(0, allImports); // set import part
		if (this.className != null) {
			fullCode.set(1, "public class " + this.className + " {"); // set class stump
		}
		// Get main-method code
		fullCode.set(3, getSplitUpExtendedCode(wholeCode)); // set written code with last } deleted
	}

	private String getSplitUpExtendedCode(String extendedCode) {
		String[] extendedCodeSplits = extendedCode.split("\\Qpublic static void main(String[] args){\\E");
		return extendedCodeSplits[1].replaceFirst("(?s)(.*)" + "}", "$1" + "");
	}

	public void getClassNameFromFile(String fileName) {
		String cn = fileName.replace(".java", "");
		className = cn;
	}

	public void saveToFile() {
		isSaved = true;
		String finalCode = getWholeCode();
		System.out.println("gesamter code:" + getWholeCode());
		try {
			FileOutputStream fos = new FileOutputStream(savefile);
			fos.write(finalCode.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getAbsolutePath() {
		return savefile.getAbsolutePath();
	}

	public String getClassPath() {
		return getAbsolutePath().replace(this.className, "").replace(".java", "");

	}

	public String getCode(CodeMode codeMode) {
		if (codeMode == CodeMode.EXTENDED) {
			return this.getExtendedCode();
		} else {
			return this.fullCode.get(3).toString();

		}
	}

	private String getExtendedCode() {
		this.fullCode.get(0).toString().replaceAll("\n", "");
		this.fullCode.get(1).toString().replaceAll("\n", "");
		this.fullCode.get(2).toString().replaceAll("\n", "");
		return this.fullCode.get(2).toString() + "\n" + "\t\t" + this.fullCode.get(3).toString().trim() + "\n"
				+ this.fullCode.get(4).toString();
	}

	private String getWholeCode() {
		this.fullCode.get(0).toString().replaceAll("\n", "");
		this.fullCode.get(1).toString().replaceAll("\n", "");
		this.fullCode.get(2).toString().replaceAll("\n", "");
		return fullCode.get(0).toString() + "\n" + fullCode.get(1).toString() + "\n\t" + fullCode.get(2).toString()
				+ "\n\t" + fullCode.get(3).toString().trim() + "\n" + fullCode.get(4).toString() + "\n"
				+ fullCode.get(5).toString();
	}

	public void setImports(String i) {
		this.fullCode.set(0, i);
	}

	public String getImports() {
		return this.fullCode.get(0).toString();
	}

	public void getClassNameFromWholeCode() {
		if (fullCode.get(3).toString().contains("public")) {
			String[] tmpContents1 = fullCode.get(3).toString().split("public");
			if (tmpContents1[1].contains("class")) {
				String[] tmpContents2 = tmpContents1[1].split("class");
				if (tmpContents2[1].contains(" ")) {
					String[] tmpContents3 = tmpContents2[1].split(" ");
					this.className = tmpContents3[1];
					fullCode.set(1, "public class " + className + " {");
				} else {
					// TODO Code konnte nicht geladen werden
					System.out.println("Code fehlerhaft");
				}
			} else {
				// TODO Code konnte nicht geladen werden
				System.out.println("Code fehlerhaft");
			}
		} else {
			// TODO Code konnte nicht geladen werden
			System.out.println("Code fehlerhaft");
		}
	}

	public void loadCodeFromWholeCode() {
		if (fullCode.get(3).toString().contains("String[] args")) {
			String[] tmpContents = fullCode.get(3).toString().split("args");
			String substring = tmpContents[1].substring(tmpContents[1].indexOf("{") + 1);
			substring = substring.replace("\t\t", "");
			substring = substring.replaceFirst("\n", "");
			// Delete last 2 }
			StringBuffer sb = new StringBuffer(substring).reverse();
			String tmpString = sb.toString().replaceFirst("}", "");
			tmpString = tmpString.replaceFirst("}", "");
			substring = new StringBuffer(tmpString).reverse().toString();
			substring = substring.replaceAll("(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)", "");
			fullCode.set(3, substring);
		}
	}

	public String getClassName() {
		return this.className;
	}

	public void writeAllCodeToArray(String text, CodeMode codeMode) {
		if (codeMode == CodeMode.EXTENDED) {
			fullCode.set(3, getSplitUpExtendedCode(text));
		} else {
			fullCode.set(3, text);
		}
	}

}
