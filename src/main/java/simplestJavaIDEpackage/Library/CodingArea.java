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
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.CodeStructure.Methods;

public class CodingArea extends JPanel {
  private static final long serialVersionUID = -3178874378975696478L;
  private static final Color ERROR_LINE_COLOR = new Color(120, 45, 45);
  private final RSyntaxTextArea syntaxTextAreaMainMethod;
  private final JButton runButton;
  private final JButton saveButton;
  private Methods method;

  public CodingArea(Methods method, JButton runButtonTmp, JButton saveButtonTmp, Font font) {
    syntaxTextAreaMainMethod = new RSyntaxTextArea(20, 60);
    RTextScrollPane scrollPaneMainMethod = new RTextScrollPane(syntaxTextAreaMainMethod);
    // Line numbers so the "Method X, line N" error references are easy to follow.
    scrollPaneMainMethod.setLineNumbersEnabled(true);
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
    syntaxTextAreaMainMethod.setCurrentLineHighlightColor(new Color(0x2A2C31));
    syntaxTextAreaMainMethod.setBackground(simplestJavaIDEpackage.Theme.EDITOR_BG);
    // Do nothing
    DocumentListener syntaxTextAreaInputListener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            saveButton.setEnabled(true);
            runButton.setEnabled(true);
            syntaxTextAreaMainMethod.removeAllLineHighlights();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            saveButton.setEnabled(true);
            runButton.setEnabled(true);
            syntaxTextAreaMainMethod.removeAllLineHighlights();
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
            boolean macOS_CTRL_Key_pressed = ((e.getModifiersEx() & KeyEvent.META_DOWN_MASK) != 0);
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
    syntaxTextAreaMainMethod.setText(method.content());
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

  /** Changes the method's name, keeping its current code. */
  public void rename(String newName) {
    this.method = new Methods(newName, method.content());
  }

  /** Highlights a 1-based line as an error and moves the caret to it. */
  public void highlightErrorLine(int localLine) {
    int lineIndex = localLine - 1;
    if (lineIndex < 0 || lineIndex >= syntaxTextAreaMainMethod.getLineCount()) {
      return;
    }
    try {
      syntaxTextAreaMainMethod.removeAllLineHighlights();
      syntaxTextAreaMainMethod.addLineHighlight(lineIndex, ERROR_LINE_COLOR);
      syntaxTextAreaMainMethod.setCaretPosition(
          syntaxTextAreaMainMethod.getLineStartOffset(lineIndex));
    } catch (BadLocationException e) {
      // Line is out of range (e.g. content changed); nothing to highlight.
    }
  }

  public void clearErrorHighlights() {
    syntaxTextAreaMainMethod.removeAllLineHighlights();
  }
}
