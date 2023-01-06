package simplestJavaIDEpackage;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;

import javax.imageio.ImageIO;
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
		frmSimplestjavaideImprint.setBounds(100, 100, 600, 600);
		frmSimplestjavaideImprint.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmSimplestjavaideImprint.setResizable(false);
		
		//Set Icon
		try {
			frmSimplestjavaideImprint.setIconImage(ImageIO.read(new File("resources/favicon.png")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		JLabel lblCredits = new JLabel("<html>\r\n\t<body style=\"margin: 15px\">\r\n\t\t<b>\r\n\t\t<p style=\"text-align:center\">\r\n\t\t© Daniel Trageser 2022 <br>\r\n\t\tContact: daniel.trageser@outlook.com<br>\r\n\t\t<br>\r\n\t\tSimplestJavaIDE is designed for usage in german vocational schools <br> in topics which have the goal to teach java without touching objects, classes etc. <br>\r\n\t\t<br>\r\n\t\tExample usage: 11th grade in Fachoberschule Informationstechnik Hessen<br>\r\n\t\t</p></b>\r\n\t\t<br>\r\n\t\t<br>\r\n\t\t<br>\r\n\t\t<br>\r\n\t\t<u>Dependencies copyright notice:</u>\r\n\t\t<p>\r\n\t\t\tCopyright (c) 2021, Robert Futrell All rights reserved.\r\n\r\nRedistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\r\n\r\n<ul><li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.</li>\r\n<li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.</li>\r\n<li>Neither the name of the author nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.</li></ul>\r\nTHIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\r\n\t\t</p>\r\n\t</body>\r\n</hml>");
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
