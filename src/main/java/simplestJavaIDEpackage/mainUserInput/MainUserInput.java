package simplestJavaIDEpackage.mainUserInput;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.swing.JFrame;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ImprintWindow;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTextField;
import javax.swing.JLabel;

public class MainUserInput {

	private JFrame frmSimplestJavaIDE;
	private JTextField textField;

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
		panelForButtonsAndInput.setLayout(new BorderLayout(0,0));

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
		
		textField = new JTextField();
		textField.setBounds(6, 58, 272, 47);
		panelButtonsRight.add(textField);
		textField.setColumns(10);
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					codingFile.is = new ByteArrayInputStream(textField.getText().getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				textField.setText(null);
			}
		});
		
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
		JTextArea textPane = new JTextArea();
		textPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textPane);
		bottomPanel.add(scrollPane, BorderLayout.CENTER);
		textPane.setForeground(new Color(255, 255, 255));
		textPane.setBackground(new Color(0, 0, 0));

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
				saveAndRun(textPane, codingArea, codingFile);
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

	public void save(RSyntaxTextArea codingArea, CodingFile codingFile) {
		codingFile.writeCodeToVariable(codingArea.getText());
		codingFile.saveToFile();
		
	}

	public void run(JTextArea outputTextPane, CodingFile codingFile) throws IOException {
		CommandExecuter ce = new CommandExecuter();
		ce.run("javac " + codingFile.getAbsolutePath(), outputTextPane, codingFile);
		ce.run("java " + "-cp " + codingFile.getClassPath(), outputTextPane, codingFile);
	}

	public void saveAndRun(JTextArea outputTextPane, RSyntaxTextArea codingArea, CodingFile codingFile) {
		outputTextPane.setText("");
		save(codingArea, codingFile);
		try {
			run(outputTextPane, codingFile);
		} catch (IOException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
	}
}
