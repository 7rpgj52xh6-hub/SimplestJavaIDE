package simplestJavaIDEpackage.mainUserInput.Terminal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.StringJoiner;

import javax.swing.JButton;

public class ProcessRunner extends Thread {
	private List<String> cmds;
	private CommandListener listener;
	private Process process;
	private JButton runButton, compileButton;

	public ProcessRunner(CommandListener listener, List<String> cmds, JButton runButton, JButton compileButton) {
		this.cmds = cmds;
		this.listener = listener;
		this.runButton = runButton;
		this.compileButton = compileButton;
		start();
	}

	public void run() {
		try {
			ProcessBuilder pb = new ProcessBuilder(cmds);
			pb.redirectErrorStream();
			process = pb.start();
			InputStream is = process.getInputStream();
			InputStream errs = process.getErrorStream();
			StreamReader reader = new StreamReader(listener, is);
			if (errs.read() != -1) {
				StreamReader errorReader = new StreamReader(listener, errs);
				errorReader.join();
				//TODO Correct error handling
				runButton.setEnabled(false);
				compileButton.setEnabled(false);
			}
			// Need a stream writer...

			int result = process.waitFor();

			// Terminate the stream writer
			reader.join();

			// TODO Change UI Bevaviour if Errors occur

			StringJoiner sj = new StringJoiner(" ");
			cmds.stream().forEach((cmd) -> {
				sj.add(cmd);
			});

			listener.commandCompleted(sj.toString(), result);
		} catch (Exception exp) {
			exp.printStackTrace();
			listener.commandFailed(exp);
		}
	}

	public void write(String text) throws IOException {
		if (process != null && process.isAlive()) {
			process.getOutputStream().write(text.getBytes());
			process.getOutputStream().flush();
		}
	}
}
