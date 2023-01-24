package simplestJavaIDEpackage.Library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
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
  private RTextScrollPane scrollPaneStandard;
  private RSyntaxTextArea syntaxTextAreaStandard;
  private RTextScrollPane scrollPaneAdvanced;
  private RSyntaxTextArea syntaxTextAreaAdvanced;
  private RTextScrollPane scrollPaneExpert;
  private RSyntaxTextArea syntaxTextAreaExpert;
  private JTabbedPane tabbedPane;
  private JButton runButton;
  private JButton saveButton;
  private CodingFile codingFile;
  private CodeMode codeMode = CodeMode.STANDARD;

  public CodingArea(CodingFile codingFileTmp, JButton runButtonTmp, JButton saveButtonTmp) {
    syntaxTextAreaStandard = new RSyntaxTextArea(20, 60);
    scrollPaneStandard = new RTextScrollPane(syntaxTextAreaStandard);
    syntaxTextAreaAdvanced = new RSyntaxTextArea(20, 60);
    scrollPaneAdvanced = new RTextScrollPane(syntaxTextAreaAdvanced);
    syntaxTextAreaExpert = new RSyntaxTextArea(20, 60);
    scrollPaneExpert = new RTextScrollPane(syntaxTextAreaExpert);
    tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Standard", scrollPaneStandard);
    tabbedPane.addTab("Advanced", scrollPaneAdvanced);
    tabbedPane.addTab("Expert", scrollPaneExpert);
    tabbedPane.addChangeListener(tabbedPaneChangeListener);
    this.setLayout(new BorderLayout());
    this.add(tabbedPane, BorderLayout.CENTER);
    this.runButton = runButtonTmp;
    this.saveButton = saveButtonTmp;
    this.codingFile = codingFileTmp;
    syntaxTextAreaStandard.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    syntaxTextAreaStandard.setCodeFoldingEnabled(true);
    syntaxTextAreaAdvanced.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    syntaxTextAreaAdvanced.setCodeFoldingEnabled(true);
    syntaxTextAreaExpert.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    syntaxTextAreaExpert.setCodeFoldingEnabled(true);
    try {
      Theme theme = Theme
          .load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
      theme.apply(syntaxTextAreaStandard);
      theme.apply(syntaxTextAreaAdvanced);
      theme.apply(syntaxTextAreaExpert);
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
    syntaxTextAreaStandard.setCurrentLineHighlightColor(new Color(55, 55, 55));
    syntaxTextAreaStandard.setBackground(new Color(47, 47, 47));
    syntaxTextAreaStandard.getDocument().addDocumentListener(syntaxTextAreaInputListener);
    syntaxTextAreaStandard.addKeyListener(inputListener);
    syntaxTextAreaAdvanced.setCurrentLineHighlightColor(new Color(55, 55, 55));
    syntaxTextAreaAdvanced.setBackground(new Color(47, 47, 47));
    syntaxTextAreaAdvanced.getDocument().addDocumentListener(syntaxTextAreaInputListener);
    syntaxTextAreaAdvanced.addKeyListener(inputListener);
    syntaxTextAreaExpert.setCurrentLineHighlightColor(new Color(55, 55, 55));
    syntaxTextAreaExpert.setBackground(new Color(47, 47, 47));
    syntaxTextAreaExpert.getDocument().addDocumentListener(syntaxTextAreaInputListener);
    syntaxTextAreaExpert.addKeyListener(inputListener);

    // Load Code if possible
    // TODO Find better solution
    boolean loadingEnabled = true;
    while (loadingEnabled) {
      if (codingFile.isFinishedProcessing) {
        loadingEnabled = false;
        syntaxTextAreaStandard.setText(codingFile.getCode(codeMode));
      }
    }
  }

  public RSyntaxTextArea getTextArea() {
    return syntaxTextAreaStandard;
  }

  public void save(CodingFile codingFile) {
    saveButton.setEnabled(false);
    switch (codeMode) {
      case STANDARD:
        codingFile.writeAllCodeToArray(syntaxTextAreaStandard.getText(), codeMode);
        break;
      case ADVANCED:
        codingFile.writeAllCodeToArray(syntaxTextAreaAdvanced.getText(), codeMode);
        break;
      case EXPERT:
        codingFile.writeAllCodeToArray(syntaxTextAreaExpert.getText(), codeMode);
        break;
      default:
        ErrorPopupWindow.throwMessage("Error with changing of code modes. Please restart.");
        break;
    }
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
      syntaxTextAreaStandard.setText(null);
      syntaxTextAreaAdvanced.setText(null);
      syntaxTextAreaExpert.setText(null);
      JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
      int newModeIndex = sourceTabbedPane.getSelectedIndex();
      switch (newModeIndex) {
        case 0:
          codeMode = CodeMode.STANDARD;
          syntaxTextAreaStandard.setText(codingFile.getCode(codeMode));
          break;
        case 1:
          codeMode = CodeMode.ADVANCED;
          syntaxTextAreaAdvanced.setText(codingFile.getCode(codeMode));
          break;
        case 2:
          codeMode = CodeMode.EXPERT;
          syntaxTextAreaExpert.setText(codingFile.getCode(codeMode));
          break;
        default:
          ErrorPopupWindow.throwMessage("Error while changing coding mode. Please restart");
          break;
      }
    }
  };
}
