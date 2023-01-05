package simplestJavaIDEpackage;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import simplestJavaIDEpackage.mainUserInput.MainUserInput;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JLabel;

public class StartingWindow {

	private JFrame frmSimplestjavaide;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StartingWindow window = new StartingWindow();
					window.frmSimplestjavaide.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public StartingWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frmSimplestjavaide = new JFrame();
		frmSimplestjavaide.setTitle("SimplestJavaIDE Alpha v1.2");
		frmSimplestjavaide.setBounds(100, 100, 450, 300);
		frmSimplestjavaide.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSimplestjavaide.setResizable(false);
		
		JPanel panel = new JPanel();
		frmSimplestjavaide.getContentPane().add(panel, BorderLayout.NORTH);

		JButton btnHelp = new JButton("Help");
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ImprintWindow.main(null);
			}
		});
		frmSimplestjavaide.getContentPane().add(btnHelp, BorderLayout.SOUTH);

		JButton btnOpenExistingCode = new JButton("Open existing code");
		panel.add(btnOpenExistingCode, BorderLayout.NORTH);
		btnOpenExistingCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setSelectedFile(new File("ExampleJavaClass.java"));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Java Code Files", "java");
				fileChooser.setFileFilter(filter);
				if (fileChooser.showOpenDialog(frmSimplestjavaide) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					CodingFile codingFile = new CodingFile(file, false); // isNewFile = true
					MainUserInput.main(null, codingFile);
					frmSimplestjavaide.dispose();
				}
			}
		});

		JButton btnNewApplication = new JButton("Code new application");
		btnNewApplication.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setSelectedFile(new File("ExampleJavaClass.java"));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Java Code Files", "java");
				fileChooser.setFileFilter(filter);
				if (fileChooser.showSaveDialog(frmSimplestjavaide) == JFileChooser.APPROVE_OPTION) {
					File tmpfile = fileChooser.getSelectedFile();
					File file = new File(tmpfile.toString().replaceAll(" ", ""));
					if (!file.exists()) {
						if (!file.toString().endsWith(".java")) {
							file = new File(file.toString() + ".java");
						}
						CodingFile codingFile = new CodingFile(file, true); // isNewFile = true
						try {
							file.createNewFile();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						MainUserInput.main(null, codingFile);
						frmSimplestjavaide.dispose();
					} else {
						//File aleady exists. Opening File instead
						File file1 = fileChooser.getSelectedFile();
						CodingFile codingFile = new CodingFile(file1, false); // isNewFile = true
						MainUserInput.main(null, codingFile);
						frmSimplestjavaide.dispose();
					}
					
				}
			}
		});
		panel.add(btnNewApplication, BorderLayout.NORTH);
		
		JLabel lblNewLabel = new JLabel("<html>\n\t<body style=\"text-align:center;\">\n\t\t<h2> SimplestJavaIDE</h2>\n\t\t<h4>Code in Java without worrying about classes!</h4>\n\t</body>\n</html>\n\t\t", SwingConstants.CENTER);
		frmSimplestjavaide.getContentPane().add(lblNewLabel, BorderLayout.CENTER);
		
		
	}

}
