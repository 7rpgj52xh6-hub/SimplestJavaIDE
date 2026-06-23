package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rtextarea.RTextScrollPane;
import simplestJavaIDEpackage.ErrorPopupWindow;
import simplestJavaIDEpackage.Library.CodeStructure.Methods;

public class CodingArea extends JPanel {
  private static final long serialVersionUID = -3178874378975696478L;
  private static final Color ERROR_LINE_COLOR = new Color(120, 45, 45);
  private static final Color EXEC_LINE_COLOR = new Color(0x2E4D38);
  private final RSyntaxTextArea syntaxTextAreaMainMethod;
  private final JButton runButton;
  private final JButton saveButton;
  private final DiagnosticParser diagnosticParser = new DiagnosticParser();
  private Methods method;
  private Runnable onEdit = () -> {};

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
    // Editor comfort: keep indentation, auto-close braces/brackets/quotes, match brackets.
    syntaxTextAreaMainMethod.setAutoIndentEnabled(true);
    syntaxTextAreaMainMethod.setCloseCurlyBraces(true);
    syntaxTextAreaMainMethod.setInsertPairedCharacters(true);
    syntaxTextAreaMainMethod.setBracketMatchingEnabled(true);
    syntaxTextAreaMainMethod.setAnimateBracketMatching(false);
    CodeCompletion.install(syntaxTextAreaMainMethod);
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
    syntaxTextAreaMainMethod.addParser(diagnosticParser); // red squiggles for compile errors
    // Do nothing
    DocumentListener syntaxTextAreaInputListener =
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            saveButton.setEnabled(true);
            runButton.setEnabled(true);
            syntaxTextAreaMainMethod.removeAllLineHighlights();
            onEdit.run();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            saveButton.setEnabled(true);
            runButton.setEnabled(true);
            syntaxTextAreaMainMethod.removeAllLineHighlights();
            onEdit.run();
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
    highlightLine(localLine, ERROR_LINE_COLOR);
  }

  /** Highlights the line currently being executed in the step debugger. */
  public void highlightExecutionLine(int localLine) {
    highlightLine(localLine, EXEC_LINE_COLOR);
  }

  private void highlightLine(int localLine, Color color) {
    int lineIndex = localLine - 1;
    if (lineIndex < 0 || lineIndex >= syntaxTextAreaMainMethod.getLineCount()) {
      return;
    }
    try {
      syntaxTextAreaMainMethod.removeAllLineHighlights();
      syntaxTextAreaMainMethod.addLineHighlight(lineIndex, color);
      syntaxTextAreaMainMethod.setCaretPosition(
          syntaxTextAreaMainMethod.getLineStartOffset(lineIndex));
    } catch (BadLocationException e) {
      // Line is out of range (e.g. content changed); nothing to highlight.
    }
  }

  public void clearErrorHighlights() {
    syntaxTextAreaMainMethod.removeAllLineHighlights();
  }

  public void setOnEdit(Runnable onEdit) {
    this.onEdit = onEdit;
  }

  /** Clears the pending red error markers (call {@link #applyErrors()} to refresh). */
  public void clearErrors() {
    diagnosticParser.notices.clear();
  }

  /** Queues a red error squiggle on a 1-based line with a hover message. */
  public void addError(int localLine, String message) {
    int lineIndex = localLine - 1;
    if (lineIndex < 0 || lineIndex >= syntaxTextAreaMainMethod.getLineCount()) {
      return;
    }
    try {
      int start = syntaxTextAreaMainMethod.getLineStartOffset(lineIndex);
      int end = syntaxTextAreaMainMethod.getLineEndOffset(lineIndex);
      int length = Math.max(1, end - start - 1);
      DefaultParserNotice notice =
          new DefaultParserNotice(diagnosticParser, message, lineIndex, start, length);
      notice.setLevel(ParserNotice.Level.ERROR);
      diagnosticParser.notices.add(notice);
    } catch (BadLocationException ignored) {
      // line vanished; skip
    }
  }

  /** Refreshes the displayed error markers. */
  public void applyErrors() {
    syntaxTextAreaMainMethod.forceReparsing(diagnosticParser);
  }

  /** A parser whose notices are pushed in from the project compiler. */
  private static final class DiagnosticParser extends AbstractParser {
    private final List<ParserNotice> notices = new ArrayList<>();

    @Override
    public ParseResult parse(RSyntaxDocument doc, String style) {
      DefaultParseResult result = new DefaultParseResult(this);
      for (ParserNotice notice : notices) {
        result.addNotice(notice);
      }
      return result;
    }
  }
}
