package simplestJavaIDEpackage.mainUserInput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.CodingArea;

public class AddImportsWindow {

  private JFrame frmImportWindow;
  private RSyntaxTextArea importArea;
  private CodingFile codingFile;
  private CodingArea codingArea;
  private Font font;

  /**
   * Launch the application.
   */
  public static void main(CodingFile codingFile, CodingArea codingArea, Font font) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          AddImportsWindow window = new AddImportsWindow(codingFile, codingArea, font);
          window.frmImportWindow.setVisible(true);
        } catch (Exception e) {
          ErrorPopupWindow.throwMessage(e.getMessage());
        }
      }
    });
  }

  /**
   * Create the application.
   */
  public AddImportsWindow(CodingFile codingFile, CodingArea codingArea, Font font) {
    this.codingFile = codingFile;
    this.codingArea = codingArea;
    this.font = font;
    initialize();
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {

    frmImportWindow = new JFrame("[Saved automatically] - Add imports with 'import ...;'");
    frmImportWindow.setBounds(100, 100, 640, 360);
    frmImportWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frmImportWindow.addWindowListener(saveOnClosing);
    frmImportWindow.setResizable(false);

    // Set Icon
    try {
      frmImportWindow
          .setIconImage(ImageIO.read(getClass().getClassLoader().getResource("favicon.png")));
    } catch (IOException e1) {
      ErrorPopupWindow.throwMessage(e1.getMessage());
    }

    importArea = new RSyntaxTextArea(20, 60);
    importArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    importArea.setCodeFoldingEnabled(true);
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
  }

  private WindowListener saveOnClosing = new WindowListener() {

    @Override
    public void windowOpened(WindowEvent e) {
      importArea.setText(codingFile.getImports());
      importArea.setFont(font);
    }

    @Override
    public void windowIconified(WindowEvent e) {
      saveImports();
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
      loadImports();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
      saveImports();
    }

    @Override
    public void windowClosing(WindowEvent e) {
      saveImports();
    }

    @Override
    public void windowClosed(WindowEvent e) {
      // do nothing
    }

    @Override
    public void windowActivated(WindowEvent e) {
      loadImports();
    }
  };

  public void saveImports() {
    String importsText = importArea.getText();
    if (!importsText.contains("import")) {
      codingFile.setImports("");
    } else {
      codingFile.setImports(importArea.getText());
    }
    codingArea.save(codingFile);
    frmImportWindow.dispose();
  }

  public void loadImports() {
    importArea.setText(codingFile.getImports());
    importArea.setFont(font);
  }
}
