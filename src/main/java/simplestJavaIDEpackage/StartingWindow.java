package simplestJavaIDEpackage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.formdev.flatlaf.FlatDarkLaf;
import simplestJavaIDEpackage.mainUserInput.MainUserInput;

public class StartingWindow {

  private JFrame frmSimplestJavaIDE_startingWindow;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          UIManager.setLookAndFeel(new FlatDarkLaf());
          UIManager.put("TextField.border", new Color(0, 0, 0, 0));
          UIManager.put("Button.border", new Color(0, 0, 0, 0));
          UIManager.put("ScrollPane.border", new Color(0, 0, 0, 0));
        } catch (Exception ex) {
          ErrorPopupWindow.throwMessage(ex.getMessage());
        }
        try {
          StartingWindow window = new StartingWindow();
          window.frmSimplestJavaIDE_startingWindow.setVisible(true);
        } catch (Exception e) {
          ErrorPopupWindow.throwMessage(e.getMessage());
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
    frmSimplestJavaIDE_startingWindow = new JFrame();
    frmSimplestJavaIDE_startingWindow.setTitle("SimplestJavaIDE Alpha v1.7");
    frmSimplestJavaIDE_startingWindow.setBounds(100, 100, 500, 350);
    frmSimplestJavaIDE_startingWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmSimplestJavaIDE_startingWindow.setResizable(false);

    // Set Icon
    try {
      frmSimplestJavaIDE_startingWindow
          .setIconImage(ImageIO.read(getClass().getClassLoader().getResource("favicon.png")));
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }

    JPanel panelAppButtons = new JPanel();
    panelAppButtons.setPreferredSize(new Dimension(0, 40));
    frmSimplestJavaIDE_startingWindow.getContentPane().add(panelAppButtons, BorderLayout.NORTH);

    JButton btnHelp = new JButton("Help");
    btnHelp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ImprintWindow.main(null);
      }
    });
    frmSimplestJavaIDE_startingWindow.getContentPane().add(btnHelp, BorderLayout.SOUTH);
    panelAppButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

    JButton btnOpenExistingCode = new JButton("Open existing code");
    btnOpenExistingCode.setPreferredSize(new Dimension(200, 30));
    panelAppButtons.add(btnOpenExistingCode);
    btnOpenExistingCode.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setPreferredSize(new Dimension(800, 500));
        fileChooser.setSelectedFile(new File("ExampleJavaClass.java"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Java Code Files", "java");
        fileChooser.setFileFilter(filter);
        if (fileChooser
            .showOpenDialog(frmSimplestJavaIDE_startingWindow) == JFileChooser.APPROVE_OPTION) {
          if (fileChooser.getSelectedFile().getAbsolutePath().contains(" ")) {
            ErrorPopupWindow.throwMessage("File path can't contain any spaces. Please reselect.");
          } else if (Character.isDigit(fileChooser.getSelectedFile().getName().charAt(0))) {
            ErrorPopupWindow.throwMessage("File name can't start with a number. Please reselect.");
          } else {
            File file = fileChooser.getSelectedFile();
            CodingFile codingFile = new CodingFile(file, false); // isNewFile = true
            MainUserInput.main(null, codingFile);
            frmSimplestJavaIDE_startingWindow.dispose();
          }

        }
      }
    });

    JButton btnNewApplication = new JButton("Code new application");
    btnNewApplication.setPreferredSize(new Dimension(200, 30));
    panelAppButtons.add(btnNewApplication);
    btnNewApplication.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setPreferredSize(new Dimension(800, 500));
        fileChooser.setSelectedFile(new File("ExampleJavaClass.java"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Java Code Files", "java");
        fileChooser.setFileFilter(filter);
        if (fileChooser
            .showSaveDialog(frmSimplestJavaIDE_startingWindow) == JFileChooser.APPROVE_OPTION) {
          if (fileChooser.getSelectedFile().getAbsolutePath().contains(" ")) {
            ErrorPopupWindow.throwMessage("File path can't contain any spaces. Please reselect.");
          } else if (Character.isDigit(fileChooser.getSelectedFile().getName().charAt(0))) {
            ErrorPopupWindow.throwMessage("File name can't start with a number. Please reselect.");
          } else {
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
              frmSimplestJavaIDE_startingWindow.dispose();
            } else {
              // File aleady exists. Opening File instead
              File file1 = fileChooser.getSelectedFile();
              CodingFile codingFile = new CodingFile(file1, false); // isNewFile = true
              MainUserInput.main(null, codingFile);
              frmSimplestJavaIDE_startingWindow.dispose();
            }
          }


        }
      }
    });

    JPanel panelCenter = new JPanel();
    frmSimplestJavaIDE_startingWindow.getContentPane().add(panelCenter, BorderLayout.CENTER);
    panelCenter.setLayout(new BorderLayout(0, 0));

    JLabel lblIcon;
    lblIcon = new JLabel();
    try {
      lblIcon = new JLabel(
          new ImageIcon(ImageIO.read(getClass().getClassLoader().getResource("favicon.png"))
              .getScaledInstance(128, 128, 0)));
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }
    lblIcon.setText("");
    lblIcon.setBorder(new EmptyBorder(10, 40, 10, 10));
    panelCenter.add(lblIcon, BorderLayout.WEST);
    JLabel lblText = new JLabel(
        "<html>\r\n\t<body style=\"text-align: center; margin:15px\">\r\n\t\t<h2> SimplestJavaIDE</h2>\r\n\t\t<h4>Code in Java without classes!</h4>\r\n\t\t<br>\r\n\t</body>\r\n</html>",
        SwingConstants.CENTER);
    panelCenter.add(lblText, BorderLayout.CENTER);

  }

}
