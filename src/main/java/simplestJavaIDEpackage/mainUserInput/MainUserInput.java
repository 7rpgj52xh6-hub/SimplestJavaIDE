package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.ImprintWindow;
import simplestJavaIDEpackage.Library.CodingArea;
import simplestJavaIDEpackage.Library.FileManager;
import simplestJavaIDEpackage.Library.Methods;
import simplestJavaIDEpackage.Library.TerminalPanel;;

/**
 *
 * @author Daniel Trageser This class implements the main user interface and its
 *         functions
 *
 */
public class MainUserInput {

	private JFrame frmSimplestJavaIDE;
	private TerminalPanel terminal;
	private JTextField userInputTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args, CodingFile savefile) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					MainUserInput window = new MainUserInput(savefile);
					window.frmSimplestJavaIDE.setVisible(true);
				} catch (Exception e) {
					ErrorPopupWindow.throwMessage(e.getMessage());
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
		frmSimplestJavaIDE = new JFrame("Simplest Java IDE - " + codingFile.getFilepath());
		frmSimplestJavaIDE.setSize(1080, 720);
		frmSimplestJavaIDE.setMinimumSize(new Dimension(1080, 720));
		frmSimplestJavaIDE.getContentPane().setLayout(new BorderLayout(0, 0));
		frmSimplestJavaIDE.addWindowListener(new WindowListener() {

			@Override
			public void windowClosing(WindowEvent arg0) {
				File javaFile = new File(codingFile.getJavaTmpFilePath());
				if (javaFile.exists()) {
					javaFile.delete();
				}
				File classFile = new File(codingFile.getJavaTmpClassPath());
				if (classFile.exists()) {
					classFile.delete();
				}

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}
		});

		// Set Icon
		try {
			frmSimplestJavaIDE.setIconImage(ImageIO.read(getClass().getClassLoader().getResource("favicon.png")));
		} catch (IOException e1) {
			ErrorPopupWindow.throwMessage(e1.getMessage());
		}

		// Structure of main window
		JSplitPane contentSplitPane = new JSplitPane();
		contentSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		frmSimplestJavaIDE.getContentPane().add(contentSplitPane, BorderLayout.CENTER);
		contentSplitPane.setResizeWeight(0.9);
		contentSplitPane.setDividerLocation(frmSimplestJavaIDE.getHeight() - 300);
		contentSplitPane.getBottomComponent().setMinimumSize(new Dimension(20000, 286));

		// Output
		JPanel bottomPanel = new JPanel();
		contentSplitPane.setBottomComponent(bottomPanel);
		bottomPanel.setPreferredSize(new Dimension(200, 286));
		bottomPanel.setLayout(new BorderLayout(0, 0));
		terminal = new TerminalPanel(userInputTextField, codingFile);
		bottomPanel.add(terminal);

		// Coding input and load code if code is not null (from loading file)
		CodingArea codingArea = new CodingArea(codingFile, terminal.getRunButton(), terminal.getSaveButton());
		contentSplitPane.setTopComponent(codingArea);

		// Action button interactions
		terminal.getHelpButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImprintWindow.main(null);
			}
		});
		terminal.getSaveButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (RSyntaxTextArea i : codingArea.getTextAreas()) {
					codingFile.methods.set(codingArea.getTextAreas().indexOf(i), new Methods(i.getText()));
				}
				for (String i : codingFile.imports) {
					// TODO Implement
				}
				if (FileManager.save(codingFile)) {
					codingFile.tmpSaveAndRunJavaCode();
					terminal.getSaveButton().setEnabled(false);
				}
			}
		});
		terminal.getRunButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Save, compile and run
				for (RSyntaxTextArea i : codingArea.getTextAreas()) {
					codingFile.methods.set(codingArea.getTextAreas().indexOf(i), new Methods(i.getText()));
				}
				for (String i : codingFile.imports) {
					// TODO Implement
				}
				if (FileManager.save(codingFile)) {
					codingFile.tmpSaveAndRunJavaCode();
					terminal.getSaveButton().setEnabled(false);
				}
				terminal.compile();

			}
		});
		terminal.getZoomInButton().getModel().addChangeListener(new ChangeListener() {
			public void zoomIn() {
				// Add Zoom
				Font font = codingArea.getTextAreas().get(0).getFont();
				if (font.getSize() <= 60) {
					for (RSyntaxTextArea i : codingArea.getTextAreas()) {
						i.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
					}
					terminal.getTextArea().setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
					terminal.getZoomOutButton().setEnabled(true);
				}
				if (font.getSize() >= 58) {
					terminal.getZoomInButton().setEnabled(false);
				}
			}

			private Timer trigger = new Timer(125, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					zoomIn();
				}
			});

			@Override
			public void stateChanged(ChangeEvent e) {
				if (terminal.getZoomInButton().getModel().isPressed()) {
					zoomIn();
					trigger.start();
				} else {
					trigger.stop();
				}
			}
		});
		terminal.getZoomOutButton().getModel().addChangeListener(new ChangeListener() {
			public void zoomOut() {
				// Subtract Zoom
				Font font = codingArea.getTextAreas().get(0).getFont();
				if (font.getSize() >= 10) {
					for (RSyntaxTextArea i : codingArea.getTextAreas()) {
						i.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
					}
					terminal.getTextArea().setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
					terminal.getZoomInButton().setEnabled(true);
				}
				if (font.getSize() <= 12) {
					terminal.getZoomOutButton().setEnabled(false);
				}
			}

			private Timer trigger = new Timer(125, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					zoomOut();
				}
			});

			@Override
			public void stateChanged(ChangeEvent e) {
				if (terminal.getZoomOutButton().getModel().isPressed()) {
					zoomOut();
					trigger.start();
				} else {
					trigger.stop();
				}
			}

		});
		terminal.getAddImportsButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddImportsWindow.main(codingFile, codingArea, codingArea.getTextAreas().get(0).getFont(), terminal);
				FileManager.save(codingFile); // Also save other code
			}
		});
	}
}
