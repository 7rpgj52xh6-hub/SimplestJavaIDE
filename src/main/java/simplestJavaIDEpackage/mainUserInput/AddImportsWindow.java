package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.CodingFile.CodeMode;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.CodingArea;

public class AddImportsWindow {

  private JFrame frmImportWindow;

  /**
   * Launch the application.
   */
  public static boolean main(CodingFile codingFile, CodingArea codingArea, CodeMode codeMode,
      Font font) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          AddImportsWindow window = new AddImportsWindow(codingFile, codingArea, codeMode, font);
          window.frmImportWindow.setVisible(true);
        } catch (Exception e) {
          ErrorPopupWindow.throwMessage(e.getMessage());
        }
      }
    });
    return false;
  }

  /**
   * Create the application.
   */
  public AddImportsWindow(CodingFile codingFile, CodingArea codingArea, CodeMode codeMode,
      Font font) {
    initialize(codingFile, codingArea, codeMode, font);
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize(CodingFile codingFile, CodingArea codingArea, CodeMode codeMode,
      Font font) {

    frmImportWindow = new JFrame();
    frmImportWindow.setBounds(100, 100, 640, 360);
    frmImportWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frmImportWindow.setResizable(false);

    // Set Icon
    try {
      frmImportWindow
          .setIconImage(ImageIO.read(getClass().getClassLoader().getResource("favicon.png")));
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }

    RSyntaxTextArea importArea = new RSyntaxTextArea(20, 60);
    importArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    importArea.setCodeFoldingEnabled(true);
    importArea.setFont(font);
    try {
      Theme theme = Theme
          .load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
      theme.apply(importArea);
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
    importArea.setCurrentLineHighlightColor(new Color(55, 55, 55));
    importArea.setBackground(new Color(47, 47, 47));
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
        codingArea.save(codingFile);
        frmImportWindow.dispose();
      }
    });
    frmImportWindow.getContentPane().add(btnSaveAndClose, BorderLayout.SOUTH);
  }
}
