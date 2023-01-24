package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.CodingFile.CodeMode;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.ImprintWindow;
import simplestJavaIDEpackage.Library.CodingArea;
import simplestJavaIDEpackage.Library.TerminalPanel;;

/**
 * 
 * @author Daniel Trageser This class implements the main user interface and its functions
 *
 */
public class MainUserInput {

  private JFrame frmSimplestJavaIDE;
  private TerminalPanel terminal;
  private CodeMode codeMode;
  private JTextField userInputTextField;

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
          ErrorPopupWindow.throwMessage(e.getMessage());
        }
      }
    });
  }

  /**
   * Create the application.
   */
  public MainUserInput(CodingFile savefile) {
    codeMode = CodeMode.STANDARD;
    initialize(savefile);
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize(CodingFile codingFile) {
    // Main Window
    frmSimplestJavaIDE = new JFrame("Simplest Java IDE - " + codingFile.getAbsolutePath());
    frmSimplestJavaIDE.setSize(1080, 720);
    frmSimplestJavaIDE.setMinimumSize(new Dimension(1080, 720));
    frmSimplestJavaIDE.getContentPane().setLayout(new BorderLayout(0, 0));

    // Set Icon
    try {
      frmSimplestJavaIDE
          .setIconImage(ImageIO.read(getClass().getClassLoader().getResource("favicon.png")));
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
    CodingArea codingArea =
        new CodingArea(codingFile, terminal.getRunButton(), terminal.getSaveButton());
    contentSplitPane.setTopComponent(codingArea);

    // Action button interactions
    terminal.getHelpButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ImprintWindow.main(null);
      }
    });
    terminal.getSaveButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        codingArea.save(codingFile);
      }
    });
    terminal.getRunButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Save, compile and run
        codingArea.save(codingFile);
        terminal.compile();
        // terminal.run();

      }
    });
    terminal.getZoomInButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Add Zoom
        if (codeMode == CodeMode.STANDARD || codeMode == CodeMode.ADVANCED
            || codeMode == CodeMode.EXPERT) {
          Font font = codingArea.getTextAreas().get(0).getFont();
          if (font.getSize() <= 60) {
            for (RSyntaxTextArea i : codingArea.getTextAreas()) {
              i.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
            }
            terminal.getTextArea()
                .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
          }
        } else {
          ErrorPopupWindow
              .throwMessage("Error with mode switch button. Mode was not set correcty.");
        }
      }
    });
    terminal.getZoomOutButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Subtract Zoom
        if (codeMode == CodeMode.STANDARD || codeMode == CodeMode.ADVANCED
            || codeMode == CodeMode.EXPERT) {
          Font font = codingArea.getTextAreas().get(0).getFont();
          if (font.getSize() >= 4) {
            for (RSyntaxTextArea i : codingArea.getTextAreas()) {
              i.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
            }
            terminal.getTextArea()
                .setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() - 2));
          }
        } else {
          ErrorPopupWindow
              .throwMessage("Error with mode switch button. Mode was not set correcty.");
        }
      }
    });
    terminal.getAddImportsButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (codeMode == CodeMode.STANDARD || codeMode == CodeMode.ADVANCED
            || codeMode == CodeMode.EXPERT) {
          AddImportsWindow.main(codingFile, codingArea, codeMode,
              codingArea.getTextAreas().get(0).getFont());
        } else {
          ErrorPopupWindow
              .throwMessage("Error with mode switch button. Mode was not set correcty.");
        }
        codingArea.save(codingFile); // Also save other code
      }
    });
  }
}
