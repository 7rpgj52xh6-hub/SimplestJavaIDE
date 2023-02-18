package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import simplestJavaIDEpackage.CodingFile;
import simplestJavaIDEpackage.CodingFile.CodeMode;
import simplestJavaIDEpackage.ErrorPopupWindow;

public class CodingArea extends JPanel {
  private static final long serialVersionUID = -3178874378975696478L;
  private RTextScrollPane scrollPaneMainMethod;
  private JPanel newMethodPanel;
  private RSyntaxTextArea syntaxTextAreaMainMethod;
  private JTabbedPane tabbedPane;
  private JButton runButton;
  private JButton saveButton;
  private CodingFile codingFile;
  private CodeMode codeMode = CodeMode.STANDARD;

  public CodingArea(CodingFile codingFileTmp, JButton runButtonTmp, JButton saveButtonTmp) {
    syntaxTextAreaMainMethod = new RSyntaxTextArea(20, 60);
    scrollPaneMainMethod = new RTextScrollPane(syntaxTextAreaMainMethod);
    newMethodPanel = new JPanel();
    tabbedPane = new JTabbedPane();
    tabbedPane.addTab("main", scrollPaneMainMethod);
    tabbedPane.addTab("Add new method", newMethodPanel);
    tabbedPane.addChangeListener(tabbedPaneChangeListener);
    this.setLayout(new BorderLayout());
    this.add(tabbedPane, BorderLayout.CENTER);
    this.runButton = runButtonTmp;
    this.saveButton = saveButtonTmp;
    this.codingFile = codingFileTmp;
    syntaxTextAreaMainMethod.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    syntaxTextAreaMainMethod.setCodeFoldingEnabled(true);
    try {
      Theme theme = Theme
          .load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
      theme.apply(syntaxTextAreaMainMethod);
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
    syntaxTextAreaMainMethod.setCurrentLineHighlightColor(new Color(55, 55, 55));
    syntaxTextAreaMainMethod.setBackground(new Color(47, 47, 47));
    syntaxTextAreaMainMethod.getDocument().addDocumentListener(syntaxTextAreaInputListener);
    syntaxTextAreaMainMethod.addKeyListener(inputListener);

    // Load Code if possible
    // TODO Find better solution
    boolean loadingEnabled = true;
    while (loadingEnabled) {
      if (codingFile.isFinishedProcessing) {
        loadingEnabled = false;
        syntaxTextAreaMainMethod.setText(codingFile.getCode(codeMode));
      }
    }
  }

  public List<RSyntaxTextArea> getTextAreas() {
    return Arrays.asList(syntaxTextAreaMainMethod);
  }

  public void save(CodingFile codingFile) {
    saveButton.setEnabled(false);
    // TODO Save
    codingFile.saveToFile();
  }

  private KeyListener inputListener = new KeyListener() {
    @Override
    public void keyTyped(KeyEvent e) {
      // do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
      // TODO Test on macos and linux
      boolean windowsCTRLpressed = (e.isControlDown());
      boolean macOSCTRLpressed = ((e.getModifiersEx() & KeyEvent.VK_META) != 0);
      if ((e.getKeyCode() == KeyEvent.VK_S) && (windowsCTRLpressed || macOSCTRLpressed)) {
        System.out.println("Test");
        save(codingFile);
      }
    }

    @Override
    public void keyReleased(KeyEvent e) {
      // do nothing
    }
  };

  private DocumentListener syntaxTextAreaInputListener = new DocumentListener() {
    @Override
    public void insertUpdate(DocumentEvent e) {
      codingFile.isSaved = false;
      saveButton.setEnabled(true);
      runButton.setEnabled(true);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      codingFile.isSaved = false;
      saveButton.setEnabled(true);
      runButton.setEnabled(true);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      // Do nothing
    }
  };

  private ChangeListener tabbedPaneChangeListener = new ChangeListener() {
    @Override
    public void stateChanged(ChangeEvent e) {
      save(codingFile);
      // TODO load contents maybe
    }
  };
}
