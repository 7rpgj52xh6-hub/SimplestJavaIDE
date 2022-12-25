package simplestJavaIDEpackage.mainUserInput;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JFrame;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ImprintWindow;
import simplestJavaIDEpackage.mainUserInput.Terminal.AppendTask;
import simplestJavaIDEpackage.mainUserInput.Terminal.Command;
import simplestJavaIDEpackage.mainUserInput.Terminal.CommandListener;
import simplestJavaIDEpackage.mainUserInput.Terminal.ProtectedDocumentFilter;
import simplestJavaIDEpackage.mainUserInput.Terminal.Terminal;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

import java.awt.Dimension;
import javax.swing.JLabel;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.BadLocationException;

public class MainUserInput implements CommandListener, Terminal {

	private JFrame frmSimplestJavaIDE;
	private JTextArea textArea;
	private int userInputStart = 0;
	private Command cmd;

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
		initialize(savefile);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(CodingFile codingFile) {
		// Main Window
		frmSimplestJavaIDE = new JFrame("Simplest Java IDE");
		frmSimplestJavaIDE.setSize(1080, 720);
		frmSimplestJavaIDE.setMinimumSize(new Dimension(1080, 720));
		frmSimplestJavaIDE.getContentPane().setLayout(new BorderLayout(0, 0));

		// Structure of main window
		JPanel bottomPanel = new JPanel();
		frmSimplestJavaIDE.getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
		bottomPanel.setPreferredSize(new Dimension(200, 216));
		bottomPanel.setLayout(new BorderLayout(0, 0));

		JPanel panelForButtonsAndInput = new JPanel();
		bottomPanel.add(panelForButtonsAndInput, BorderLayout.LINE_START);
		panelForButtonsAndInput.setMaximumSize(new Dimension(284, 181));
		panelForButtonsAndInput.setMinimumSize(new Dimension(284, 181));
		panelForButtonsAndInput.setPreferredSize(new Dimension(284, 181));
		panelForButtonsAndInput.setLayout(new BorderLayout(0, 0));

		JPanel panelButtonsLeft = new JPanel();
		panelForButtonsAndInput.add(panelButtonsLeft, BorderLayout.NORTH);
		panelButtonsLeft.setMaximumSize(new Dimension(284, 111));
		panelButtonsLeft.setMinimumSize(new Dimension(284, 111));
		panelButtonsLeft.setPreferredSize(new Dimension(284, 111));
		panelButtonsLeft.setLayout(null);

		JPanel panelButtonsRight = new JPanel();
		panelForButtonsAndInput.add(panelButtonsRight, BorderLayout.SOUTH);
		panelButtonsRight.setMaximumSize(new Dimension(284, 111));
		panelButtonsRight.setMinimumSize(new Dimension(284, 111));
		panelButtonsRight.setPreferredSize(new Dimension(284, 111));
		panelButtonsRight.setLayout(null);

		JLabel lblNewLabel = new JLabel("User Input:");
		lblNewLabel.setBounds(6, 30, 272, 16);
		panelButtonsRight.add(lblNewLabel);

		// Buttons
		JButton btnSave = new JButton("Save");
		btnSave.setBounds(6, 41, 130, 29);
		btnSave.setEnabled(false);
		panelButtonsLeft.add(btnSave);

		JButton btnSaveAndRun = new JButton("Save & Run");
		btnSaveAndRun.setBounds(6, 76, 130, 29);
		panelButtonsLeft.add(btnSaveAndRun);

		JButton btnAddImports = new JButton("Add imports");
		btnAddImports.setBounds(6, 6, 130, 29);
		panelButtonsLeft.add(btnAddImports);

		JButton btnHelp = new JButton("Help");
		btnHelp.setIcon(null);
		btnHelp.setBounds(142, 41, 130, 29);
		panelButtonsLeft.add(btnHelp);

		JButton btnClose = new JButton("Close");
		btnClose.setIcon(null);
		btnClose.setBounds(142, 76, 130, 29);
		panelButtonsLeft.add(btnClose);

		JButton btnShowAllCode = new JButton("View full code");
		btnShowAllCode.setBounds(142, 6, 130, 29);
		panelButtonsLeft.add(btnShowAllCode);

		// Coding input and load code if code is not null (from loading file)
		RSyntaxTextArea codingArea = new RSyntaxTextArea(20, 60);
		codingArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		codingArea.setCodeFoldingEnabled(true);
		codingArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				codingFile.isSaved = false;
				btnSave.setEnabled(true);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				// Do Nothing

			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// Do Nothing

			}
		});
		RTextScrollPane codingAreaScrollPane = new RTextScrollPane(codingArea);
		frmSimplestJavaIDE.getContentPane().add(codingAreaScrollPane, BorderLayout.CENTER);
		// Load Code if possible
		boolean loadingEnabled = true;
		while (loadingEnabled) {
			if (codingFile.isFinishedProcessing) {
				loadingEnabled = false;
				codingArea.setText(codingFile.getCode());
			}
		}

		// Output

		cmd = new Command(this);

		textArea = new JTextArea(20, 30);
		textArea.setBackground(Color.BLACK);
		textArea.setForeground(Color.WHITE);
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
				//HIER WIRD AUSGEFÜHRT WAS IN DER CMD STEHT
				int range = textArea.getCaretPosition() - userInputStart;
				try {
					String text = textArea.getText(userInputStart, range).trim();
					System.out.println("[" + text + "]");
					userInputStart += range;
					if (!cmd.isRunning()) {
						cmd.execute(text);
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

		// Manage Button interactions
		btnShowAllCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save(codingArea, codingFile);
				ShowFullCodeWindow.main(null, codingFile.getWholeCode());
			}
		});
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmSimplestJavaIDE.dispose();
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
		btnSaveAndRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAndRun(textArea, codingArea, codingFile);
				btnSave.setEnabled(false);
			}
		});
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save(codingArea, codingFile);
				btnSave.setEnabled(false);
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
		//appendText("\n> " + cmd + " exited with " + result + "\n");
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
		codingFile.writeCodeToVariable(codingArea.getText());
		codingFile.saveToFile();

	}

	public void run(String command) throws IOException, BadLocationException {
		if (!cmd.isRunning()) {
			cmd.execute(command);
		} else {
			try {
				cmd.send(command + "\n");
			} catch (IOException ex) {
				appendText("!! Failed to send command to process: " + ex.getMessage() + "\n");
			}
		}
	}

	public void saveAndRun(JTextArea outputTextPane, RSyntaxTextArea codingArea, CodingFile codingFile) {
		save(codingArea, codingFile);
		try {
			outputTextPane.setText("");
			run("java --version");
			//run("javac " + codingFile.getAbsolutePath());
			//run("java " + "-cp " + codingFile.getClassPath());
		} catch (IOException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
	}
}
