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

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args, CodingFile codingFile) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AddImportsWindow window = new AddImportsWindow(codingFile);
					window.frame.setVisible(true);
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
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		RSyntaxTextArea codingArea = new RSyntaxTextArea(20, 60);
		codingArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		codingArea.setCodeFoldingEnabled(true);
		RTextScrollPane codingAreaScrollPane = new RTextScrollPane(codingArea);
		frame.getContentPane().add(codingAreaScrollPane, BorderLayout.CENTER);
		codingArea.setText(codingFile.getImports());

		JButton btnSaveAndClose = new JButton("Save and close");
		btnSaveAndClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String importsText = codingArea.getText();
				if (!importsText.contains("import")) {
					codingFile.setImports("");
				} else {
					codingFile.setImports(codingArea.getText());
				}
				frame.dispose();
			}

		});
		frame.getContentPane().add(btnSaveAndClose, BorderLayout.SOUTH);
	}

}
