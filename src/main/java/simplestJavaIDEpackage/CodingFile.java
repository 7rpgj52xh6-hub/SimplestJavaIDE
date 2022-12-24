package simplestJavaIDEpackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

public class CodingFile {
	File savefile;
	String imports;
	String className;
	String classAndMain;
	String code;
	String endOfCode = "\t}\n}";
	public boolean isFinishedProcessing = false;
	public boolean isSaved = false;
	public InputStream is;

	CodingFile(File sf, Boolean isNewFile) {
		if (!isNewFile) {
			// TOTO Auslesen von Imports, Classname, classAndMain
			StringBuilder contentBuilder = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new FileReader(sf.getAbsolutePath()))) {

				String sCurrentLine;
				while ((sCurrentLine = br.readLine()) != null) {
					contentBuilder.append(sCurrentLine).append("\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			code = contentBuilder.toString();
			loadImportsFromWholeCode();
			loadClassNameFromWholeCode();
			loadCodeFromWholeCode();
			isFinishedProcessing = true;
		} else {
			loadClassAndMainFromFileName(sf.getName());
			code = "System.out.println(\"Hello World\");";
			isFinishedProcessing = true;
		}
		savefile = sf;

	}

	public void loadClassAndMainFromFileName(String fileName) {
		String cn = fileName.replace(".java", "");
		className = cn;
		classAndMain = "public class " + cn + " {" + "\n\t"
				+ "public static void main(String[] args){";
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
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
	}
	
	public String getAbsolutePath() {
		return savefile.getAbsolutePath();
	}
	
	public String getClassPath() {
		return getAbsolutePath().replace(this.className, "").replace(".java", "") + " " + this.className;
		
	}

	public String getCode() {
		return this.code;
	}

	public String getWholeCode() {
		if (this.imports != null) {
			return this.imports + "\n" + this.classAndMain + "\n" + "\t\t" + this.code.replace("\n", "\n\t\t") + "\n"
					+ this.endOfCode;
		} else {
			return this.classAndMain + "\n" + "\t\t" + this.code.replace("\n", "\n\t\t") + "\n" + this.endOfCode;
		}

	}

	public void writeCodeToVariable(String c) {
		this.code = c;
	}

	public void setImports(String i) {
		this.imports = i;
	}

	public String getImports() {
		return this.imports;
	}

	public void loadImportsFromWholeCode() {
		if (code.contains("public")) {
			String[] contents = code.split("public");
			imports = contents[0];
		}
	}

	public void loadClassNameFromWholeCode() {
		if (code.contains("public")) {
			String[] tmpContents1 = code.split("public");
			if (tmpContents1[1].contains("class")) {
				String[] tmpContents2 = tmpContents1[1].split("class");
				if (tmpContents2[1].contains(" ")) {
					String[] tmpContents3 = tmpContents2[1].split(" ");
					this.className = tmpContents3[1];
					classAndMain = "public class " + className + " {" + "\n\t"
							+ "public static void main(String[] args){";
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
		if (code.contains("String[] args")) {
			String[] tmpContents = code.split("args");
			String substring = tmpContents[1].substring(tmpContents[1].indexOf("{") + 1);
			substring = substring.replace("\t\t", "");
			substring = substring.replaceFirst("\n", "");
			// Delete last 2 }
			StringBuffer sb = new StringBuffer(substring).reverse();
			String tmpString = sb.toString().replaceFirst("}", "");
			tmpString = tmpString.replaceFirst("}", "");
			substring = new StringBuffer(tmpString).reverse().toString();
			substring = substring.replaceAll("(?m)^\\s*\\r?\\n|\\r?\\n\\s*(?!.*\\r?\\n)", "");
			code = substring;
		}
	}

}
