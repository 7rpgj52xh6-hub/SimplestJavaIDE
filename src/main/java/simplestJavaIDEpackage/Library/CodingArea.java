package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class CodingArea extends JPanel {
  private static final long serialVersionUID = -3178874378975696478L;
  private final RSyntaxTextArea syntaxTextAreaMainMethod;
  private final JButton runButton;
  private final JButton saveButton;
  private final Methods method;

  public CodingArea(Methods method, JButton runButtonTmp, JButton saveButtonTmp, Font font) {
    syntaxTextAreaMainMethod = new RSyntaxTextArea(20, 60);
    RTextScrollPane scrollPaneMainMethod = new RTextScrollPane(syntaxTextAreaMainMethod);
    this.setLayout(new BorderLayout());
    this.add(scrollPaneMainMethod, BorderLayout.CENTER);
    this.runButton = runButtonTmp;
    this.saveButton = saveButtonTmp;
    this.method = method;
    syntaxTextAreaMainMethod.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    syntaxTextAreaMainMethod.setCodeFoldingEnabled(true);
    try {
      Theme theme =
          Theme.load(
              getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
      theme.apply(syntaxTextAreaMainMethod);
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
    syntaxTextAreaMainMethod.setCurrentLineHighlightColor(new Color(55, 55, 55));
    syntaxTextAreaMainMethod.setBackground(new Color(47, 47, 47));
    // Do nothing
    DocumentListener syntaxTextAreaInputListener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            saveButton.setEnabled(true);
            runButton.setEnabled(true);
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            saveButton.setEnabled(true);
            runButton.setEnabled(true);
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            // Do nothing
          }
        };
    syntaxTextAreaMainMethod.getDocument().addDocumentListener(syntaxTextAreaInputListener);
    // do nothing
    // TODO Test on macos and linux
    // FileManager.save(codingFile); //TODO Fix Saving
    // do nothing
    KeyListener inputListener =
        new KeyListener() {
          @Override
          public void keyTyped(KeyEvent e) {
            // do nothing
          }

          @Override
          public void keyPressed(KeyEvent e) {
            // TODO Test on macos and linux
            boolean windows_CTRL_Key_pressed = (e.isControlDown());
            boolean macOS_CTRL_Key_pressed = ((e.getModifiersEx() & KeyEvent.VK_META) != 0);
            if ((e.getKeyCode() == KeyEvent.VK_S)
                && (windows_CTRL_Key_pressed || macOS_CTRL_Key_pressed)) {
              System.out.println("Test");
              // FileManager.save(codingFile); //TODO Fix Saving
            }
          }

          @Override
          public void keyReleased(KeyEvent e) {
            // do nothing
          }
        };
    syntaxTextAreaMainMethod.addKeyListener(inputListener);
    syntaxTextAreaMainMethod.setText(method.getContent());
    if (font != null) {
      if (font.getSize() > 8 && font.getSize() < 60) {
        this.getTextArea().setFont(font);
      }
    }
  }

  public RSyntaxTextArea getTextArea() {
    return syntaxTextAreaMainMethod;
  }

  public Methods getMethod() {
    return method;
  }
}
