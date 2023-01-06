package simplestJavaIDEpackage;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import simplestJavaIDEpackage.mainUserInput.MainUserInput;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;

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
		frmSimplestjavaide.setTitle("SimplestJavaIDE Alpha v1.3");
		frmSimplestjavaide.setBounds(100, 100, 359, 298);
		frmSimplestjavaide.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSimplestjavaide.setResizable(false);

		// Set Icon
		try {
			frmSimplestjavaide.setIconImage(ImageIO.read(getClass().getClassLoader().getResource("favicon.png")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JPanel panelAppButtons = new JPanel();
		panelAppButtons.setPreferredSize(new Dimension(0,40));
		frmSimplestjavaide.getContentPane().add(panelAppButtons, BorderLayout.NORTH);

		JButton btnHelp = new JButton("Help");
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ImprintWindow.main(null);
			}
		});
		frmSimplestjavaide.getContentPane().add(btnHelp, BorderLayout.SOUTH);
		panelAppButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnOpenExistingCode = new JButton("Open existing code");
		btnOpenExistingCode.setPreferredSize(new Dimension(155,30));
		panelAppButtons.add(btnOpenExistingCode);
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
		btnNewApplication.setPreferredSize(new Dimension(155,30));
		panelAppButtons.add(btnNewApplication);
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
						// File aleady exists. Opening File instead
						File file1 = fileChooser.getSelectedFile();
						CodingFile codingFile = new CodingFile(file1, false); // isNewFile = true
						MainUserInput.main(null, codingFile);
						frmSimplestjavaide.dispose();
					}

				}
			}
		});

		JPanel panelCenter = new JPanel();
		frmSimplestjavaide.getContentPane().add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new BorderLayout(0, 0));
		
		try {
			JLabel lblIcon;
			lblIcon = new JLabel(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("favicon.png")).getScaledInstance(128, 128, 0)));
			lblIcon.setText("");
			Border margin = new EmptyBorder(10,10,10,10);
			lblIcon.setBorder(margin);
			panelCenter.add(lblIcon, BorderLayout.WEST);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JLabel lblText = new JLabel(
				"<html>\r\n\t<body style=\"text-align: center; margin:15px\">\r\n\t\t<h2> SimplestJavaIDE</h2>\r\n\t\t<h4>Code in Java without classes!</h4>\r\n\t\t<br>\r\n\t</body>\r\n</html>", SwingConstants.CENTER);
		panelCenter.add(lblText, BorderLayout.EAST);
		

	}

}
