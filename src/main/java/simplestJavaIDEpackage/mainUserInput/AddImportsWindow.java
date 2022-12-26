package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import simplestJavaIDEpackage.CodingFile;

import javax.swing.JButton;

public class AddImportsWindow {

	private JFrame frmImportWindow;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args, CodingFile codingFile) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AddImportsWindow window = new AddImportsWindow(codingFile);
					window.frmImportWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AddImportsWindow(CodingFile codingFile) {
		initialize(codingFile);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(CodingFile codingFile) {
		frmImportWindow = new JFrame();
		frmImportWindow.setBounds(100, 100, 640, 360);
		frmImportWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmImportWindow.setResizable(false);

		RSyntaxTextArea importArea = new RSyntaxTextArea(20, 60);
		importArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		importArea.setCodeFoldingEnabled(true);
		RTextScrollPane importAreaScrollPane = new RTextScrollPane(importArea);
		frmImportWindow.getContentPane().add(importAreaScrollPane, BorderLayout.CENTER);
		importArea.setText(codingFile.getImports());

		JButton btnSaveAndClose = new JButton("Save imports and close");
		btnSaveAndClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String importsText = importArea.getText();
				if (!importsText.contains("import")) {
					codingFile.setImports("");
				} else {
					codingFile.setImports(importArea.getText());
				}
				frmImportWindow.dispose();
			}

		});
		frmImportWindow.getContentPane().add(btnSaveAndClose, BorderLayout.SOUTH);
	}

}
