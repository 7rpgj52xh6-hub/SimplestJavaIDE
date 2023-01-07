package simplestJavaIDEpackage.mainUserInput;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import javax.swing.JFrame;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.CodingFile.CodeMode;
import simplestJavaIDEpackage.ImprintWindow;
import simplestJavaIDEpackage.mainUserInput.Terminal.AppendTask;
import simplestJavaIDEpackage.mainUserInput.Terminal.Command;
import simplestJavaIDEpackage.mainUserInput.Terminal.CommandListener;
import simplestJavaIDEpackage.mainUserInput.Terminal.ProtectedDocumentFilter;
import simplestJavaIDEpackage.mainUserInput.Terminal.Terminal;

import java.awt.BorderLayout;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.BadLocationException;

public class MainUserInput implements CommandListener, Terminal {

	private JFrame frmSimplestJavaIDE;
	private JTextArea textArea;
	private int userInputStart = 0;
	private Command cmd;

	private CodeMode codeMode;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args, CodingFile savefile) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainUserInput window = new MainUserInput(savefile);
					window.frmSimplestJavaIDE.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainUserInput(CodingFile savefile) {
		codeMode = CodeMode.STANDARD;
		initialize(savefile);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(CodingFile codingFile) {
		// Main Window
		frmSimplestJavaIDE = new JFrame("Simplest Java IDE - " + codingFile.getAbsolutePath());
		frmSimplestJavaIDE.setSize(1080, 720);
		frmSimplestJavaIDE.setMinimumSize(new Dimension(1080, 720));
		frmSimplestJavaIDE.getContentPane().setLayout(new BorderLayout(0, 0));

		// Set Icon
		try {
			frmSimplestJavaIDE.setIconImage(ImageIO.read(getClass().getClassLoader().getResource("favicon.png")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Structure of main window
		JPanel bottomPanel = new JPanel();
		frmSimplestJavaIDE.getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
		bottomPanel.setPreferredSize(new Dimension(200, 168));
		bottomPanel.setLayout(new BorderLayout(0, 0));

		JPanel panelButtons = new JPanel();
		bottomPanel.add(panelButtons, BorderLayout.LINE_START);
		panelButtons.setPreferredSize(new Dimension(278, 168));
		panelButtons.setLayout(null);

		// Buttons
		JButton btnSave = new JButton("Save");
		btnSave.setBounds(6, 48, 130, 36);
		btnSave.setEnabled(false);
		panelButtons.add(btnSave);

		JButton btnAddImports = new JButton("Add imports");
		btnAddImports.setBounds(6, 6, 130, 36);
		panelButtons.add(btnAddImports);

		JButton btnHelp = new JButton("Help");
		btnHelp.setIcon(null);
		btnHelp.setBounds(142, 48, 130, 36);
		panelButtons.add(btnHelp);

		JToggleButton btnShowAllCode = new JToggleButton("Advanced Mode");
		btnShowAllCode.setBounds(142, 6, 130, 36);
		panelButtons.add(btnShowAllCode);

		JButton btnRun = new JButton("Run");
		btnRun.setBounds(142, 90, 130, 72);
		btnRun.setEnabled(false);
		panelButtons.add(btnRun);

		JButton btnCompile = new JButton("Compile");
		btnCompile.setBounds(6, 90, 130, 72);
		panelButtons.add(btnCompile);

		// Coding input and load code if code is not null (from loading file)
		RSyntaxTextArea codingArea = new RSyntaxTextArea(20, 60);
		codingArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		codingArea.setCodeFoldingEnabled(true);
		codingArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// Do nothing
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				codingFile.isSaved = false;
				btnSave.setEnabled(true);
				btnCompile.setEnabled(true);
				btnRun.setEnabled(false);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				codingFile.isSaved = false;
				btnSave.setEnabled(true);
				btnCompile.setEnabled(true);
				btnRun.setEnabled(false);
			}
		});
		codingArea.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				boolean windowsCTRLpressed = ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0);
				boolean macOSCTRLpressed = ((e.getModifiersEx() & KeyEvent.VK_META) != 0);
				if ((e.getKeyCode() == KeyEvent.VK_S) && (windowsCTRLpressed || macOSCTRLpressed)) {
					save(codingArea, codingFile);
					btnSave.setEnabled(false);
				}

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Automatisch generierter Methodenstub

			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Automatisch generierter Methodenstub

			}
		});

		RTextScrollPane codingAreaScrollPane = new RTextScrollPane(codingArea);
		frmSimplestJavaIDE.getContentPane().add(codingAreaScrollPane, BorderLayout.CENTER);
		// Load Code if possible
		boolean loadingEnabled = true;
		while (loadingEnabled) {
			if (codingFile.isFinishedProcessing) {
				loadingEnabled = false;
				codingArea.setText(codingFile.getCode(codeMode));
			}
		}

		// Output
		cmd = new Command(this);
		textArea = new JTextArea(20, 30);
		((AbstractDocument) textArea.getDocument()).setDocumentFilter(new ProtectedDocumentFilter(this));
		bottomPanel.add(new JScrollPane(textArea));

		@SuppressWarnings("unused")
		InputMap im = textArea.getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap am = textArea.getActionMap();

		Action oldAction = am.get("insert-break");
		am.put("insert-break", new AbstractAction() {
			private static final long serialVersionUID = 5489224113004830522L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// HIER WIRD AUSGEFÜHRT WAS IN DER CMD STEHT
				int range = textArea.getCaretPosition() - userInputStart;
				try {
					String text = textArea.getText(userInputStart, range).trim();
					userInputStart += range;
					if (!cmd.isRunning()) {
						cmd.execute(text, btnRun, btnCompile);
					} else {
						try {
							cmd.send(text + "\n");
						} catch (IOException ex) {
							appendText("!! Failed to send command to process: " + ex.getMessage() + "\n");
						}
					}
				} catch (BadLocationException ex) {
					Logger.getLogger(MainUserInput.class.getName()).log(Level.SEVERE, null, ex);
				}
				oldAction.actionPerformed(e);
			}
		});

		// Manage interactions
		btnShowAllCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save(codingArea, codingFile);
				if (codeMode == CodeMode.STANDARD) {
					codeMode = CodeMode.EXTENDED;
					codingArea.setText(null);
					codingArea.append(codingFile.getCode(codeMode));
				} else {
					codeMode = CodeMode.STANDARD;
					codingArea.setText(null);
					codingArea.append(codingFile.getCode(codeMode).replaceFirst("\n", ""));
				}
			}
		});
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ImprintWindow.main(null);
			}
		});
		btnAddImports.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddImportsWindow.main(null, codingFile);
			}
		});
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save(codingArea, codingFile);
				btnSave.setEnabled(false);
			}
		});
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// RUN AND SAVE
				save(codingArea, codingFile);
				textArea.append("Running Application...\n");
				runApplication(textArea, codingArea, codingFile, btnRun, btnCompile);
				btnSave.setEnabled(false);
			}
		});
		btnCompile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// COMPILE AND SAVE
				save(codingArea, codingFile);
				compile(textArea, codingArea, codingFile, btnRun, btnCompile);
				textArea.append("Compiling Code...!\n");
				btnSave.setEnabled(false);
				btnRun.setEnabled(true);
				btnCompile.setEnabled(false);
			}
		});
	}

	@Override
	public void commandOutput(String text) {
		SwingUtilities.invokeLater(new AppendTask(this, text));
	}

	@Override
	public void commandFailed(Exception exp) {
		SwingUtilities.invokeLater(new AppendTask(this, "Command failed - " + exp.getMessage()));
	}

	@Override
	public void commandCompleted(String cmd, int result) {
		// appendText("\n> " + cmd + " exited with " + result + "\n");
		appendText("\n");
	}

	protected void updateUserInputPos() {
		int pos = textArea.getCaretPosition();
		textArea.setCaretPosition(textArea.getText().length());
		userInputStart = pos;

	}

	@Override
	public int getUserInputStart() {
		return userInputStart;
	}

	@Override
	public void appendText(String text) {
		textArea.append(text);
		updateUserInputPos();
	}

	public void save(RSyntaxTextArea codingArea, CodingFile codingFile) {
		codingFile.writeAllCodeToArray(codingArea.getText(), codeMode);
		codingFile.saveToFile();
	}

	public void runCommand(String command, JButton runButton, JButton compileButton)
			throws IOException, BadLocationException {
		if (!cmd.isRunning()) {
			cmd.execute(command, runButton, compileButton);
		} else {
			try {
				cmd.send(command + "\n");
			} catch (IOException ex) {
				appendText("!! Failed to send command to process: " + ex.getMessage() + "\n");
			}
		}
	}

	public void compile(JTextArea outputTextPane, RSyntaxTextArea codingArea, CodingFile codingFile, JButton runButton,
			JButton compileButton) {
		try {
			runCommand("javac " + codingFile.getAbsolutePath(), runButton, compileButton);
		} catch (IOException | BadLocationException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
	}

	public void runApplication(JTextArea outputTextPane, RSyntaxTextArea codingArea, CodingFile codingFile,
			JButton runButton, JButton compileButton) {
		try {
			runCommand("java -cp " + codingFile.getClassPath() + " " + codingFile.getClassName(), runButton,
					compileButton);
		} catch (IOException | BadLocationException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
	}

}
