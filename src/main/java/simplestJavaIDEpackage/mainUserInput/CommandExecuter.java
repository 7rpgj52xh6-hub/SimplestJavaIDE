package simplestJavaIDEpackage.mainUserInput;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.swing.JTextArea;

import simplestJavaIDEpackage.CodingFile;

public class CommandExecuter {

	public void run(String cmd, JTextArea outputField, CodingFile codingFile) {
		if (codingFile.getWholeCode().contains("new Scanner(System.in)")) {
			codingFile.getWholeCode().replaceAll("new Scanner(System.in)", "new Scanner("+codingFile.is+")");
		}
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader readerInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader readerError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			//BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			String outputLine;
			while ((outputLine = readerInput.readLine()) != null) {
				outputField.append(outputLine + "\n");
			}
			while ((outputLine = readerError.readLine()) != null) {
				outputField.append(outputLine + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
