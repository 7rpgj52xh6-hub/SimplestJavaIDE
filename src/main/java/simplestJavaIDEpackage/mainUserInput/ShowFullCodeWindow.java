package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;


public class ShowFullCodeWindow {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args, String code) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ShowFullCodeWindow window = new ShowFullCodeWindow(code);
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
	public ShowFullCodeWindow(String code) {
		initialize(code);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String code) {
		frame = new JFrame();
		frame.setBounds(100, 100, 1080, 720);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		RSyntaxTextArea codingArea = new RSyntaxTextArea(20, 60);
		codingArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		codingArea.setCodeFoldingEnabled(true);
		codingArea.setText(code);
		codingArea.setEditable(false);
		RTextScrollPane codingAreaScrollPane = new RTextScrollPane(codingArea);
		frame.getContentPane().add(codingAreaScrollPane, BorderLayout.CENTER);
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		frame.getContentPane().add(btnClose, BorderLayout.SOUTH);
	}

}
