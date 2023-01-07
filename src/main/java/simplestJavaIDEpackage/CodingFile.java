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
		STANDARD, EXTENDED, FULL
	}

	CodingFile(File sf, Boolean isNewFile) {
		fullCode = new ArrayList<String>();
		fullCode.add(""); // Imports
		fullCode.add(""); // ClassStump
		fullCode.add("public static void main(String[] args){"); // MainMethodStump
		fullCode.add(""); // WrittenCode
		fullCode.add("\t}"); // End of Method(s)
		fullCode.add("}"); // End of Class
		getClassNameFromFile(sf.getName());
		if (!isNewFile) {
			loadCodeFromFile(sf);
			isFinishedProcessing = true;
		} else {
			fullCode.set(3, "System.out.println(\"Hello World\");");
			fullCode.set(1, "public class " + this.className + " {"); // Set class stump
			isFinishedProcessing = true;
		}
		savefile = sf;
	}

	private String getStandardCodeFromFullOrExtended(String extendedCode) {
		if (extendedCode.contains("public class")) {
			String toBeDeleted = extendedCode.substring(extendedCode.indexOf("public class"),
					extendedCode.indexOf("\n"));
			extendedCode = extendedCode.replace(toBeDeleted, "").replaceFirst("\n", "").replaceFirst("(?s)(.*)" + "}",
					"$1" + "");
		}
		if (extendedCode.contains("public static void main(String[] args){")) {
			return extendedCode.replace("public static void main(String[] args){", "").replaceFirst("(?s)(.*)" + "}",
					"$1" + "");
		} else {
			// TODO Correct error handling
			return "Error";
		}

	}

	public void getClassNameFromFile(String fileName) {
		String cn = fileName.replace(".java", "");
		className = cn;
	}

	public void saveToFile() {
		isSaved = true;
		String finalCode = getWholeCode();
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
		switch (codeMode) {
		case STANDARD:
			return this.getWrittenCode(0);
		case EXTENDED:
			return this.getExtendedCode();
		case FULL:
			return this.getWholeCode();
		default:
			// TODO Correct error handling
			return null;
		}
	}

	private String getWrittenCode(Integer amountOfTabs) {
		String source = fullCode.get(3).toString();
		List<String> output = new ArrayList<String>();
		if (source.contains("\n")) {
			String[] linesOfWrittenCode = source.split("\n");
			for (String s : linesOfWrittenCode) {
				for (int i = 0; i < amountOfTabs; i++) {
					s = "\t" + s;
				}
				output.add(s);
			}
			source = String.join("\n", output);
		} else {
			for (int i = 0; i < amountOfTabs; i++) {
				source = "\t" + source;
			}
		}
		return source;
	}

	private void setWrittenCode(String input) {
		if (input.contains("\n")) {
			String[] inputLines = input.split("\n");
			String result = "";
			for (String i : inputLines) {
				i = i.replaceAll("\t", "");
				result = result + i + "\n";
			}
			fullCode.set(3, result);
		} else {
			fullCode.set(3, input.replaceAll("\t", ""));
		}
	}

	private String getExtendedCode() {
		return this.fullCode.get(2).toString() + "\n\t" + getWrittenCode(1).trim() + "\n"
				+ this.fullCode.get(4).toString().replaceFirst("(?s)(.*)" + "\\t", "$1" + "");
	}

	private String getWholeCode() {
		if (this.fullCode.get(0).toString().contains("import")) { // different if imports exist (additional new line)
			return fullCode.get(0).toString() + "\n" + fullCode.get(1).toString() + "\n\t" + fullCode.get(2).toString()
					+ getWrittenCode(2) + "\n" + fullCode.get(4).toString() + "\n" + fullCode.get(5).toString();
		} else {
			return fullCode.get(1).toString() + "\n\t" + fullCode.get(2).toString() + getWrittenCode(2) + "\n"
					+ fullCode.get(4).toString() + "\n" + fullCode.get(5).toString();
		}
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
				}
			}
		}
	}

	public void loadCodeFromFile(File file) {

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

		String[] wholeCodeInLines = wholeCode.split("\n");
		String allImports = "";
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

		setWrittenCode(getStandardCodeFromFullOrExtended(wholeCode).replaceFirst("(?s)(.*)" + "}", "$1" + "")
				.replaceAll("(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)", "")); // set written code with last } deleted
	}

	public String getClassName() {
		return this.className;
	}

	public void writeAllCodeToArray(String text, CodeMode codeMode) {
		switch (codeMode) {
		case EXTENDED:
			setWrittenCode(getStandardCodeFromFullOrExtended(text));
			break;
		case STANDARD:
			setWrittenCode(text);
			break;
		case FULL:
			setWrittenCode(getStandardCodeFromFullOrExtended(text));
			break;
		default:
			// TODO Correct error handling
			break;
		}
	}
}