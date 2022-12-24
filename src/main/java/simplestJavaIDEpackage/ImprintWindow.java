package simplestJavaIDEpackage;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import javax.swing.JButton;

public class ImprintWindow {

	private JFrame frmSimplestjavaideImprint;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ImprintWindow window = new ImprintWindow();
					window.frmSimplestjavaideImprint.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ImprintWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmSimplestjavaideImprint = new JFrame();
		frmSimplestjavaideImprint.setTitle("SimplestJavaIDE - Imprint");
		frmSimplestjavaideImprint.setAlwaysOnTop(true);
		frmSimplestjavaideImprint.setBounds(100, 100, 450, 300);
		frmSimplestjavaideImprint.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSimplestjavaideImprint.setResizable(false);
		
		JLabel lblCredits = new JLabel("<html>\n\t<body style=\"text-align:center\">\n\t\t© Daniel Trageser 2022 <br>\n\t\tContact: daniel.trageser@outlook.com<br>\n\t\t<br>\n\t\tSimplestJavaIDE is designed for usage in german vocational schools <br> in topics which have the goal to teach java without touching objects, classes etc. <br>\n\t\t<br>\n\t\tExample usage: 11th grade in Fachoberschule Informationstechnik Hessen<br>\n\t</body>\n</hml>");
		frmSimplestjavaideImprint.getContentPane().add(lblCredits, BorderLayout.CENTER);
		
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				frmSimplestjavaideImprint.dispose();
			}
		});
		frmSimplestjavaideImprint.getContentPane().add(btnClose, BorderLayout.SOUTH);
	}

}
