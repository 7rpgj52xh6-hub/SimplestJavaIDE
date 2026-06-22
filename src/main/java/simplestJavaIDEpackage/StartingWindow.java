package simplestJavaIDEpackage;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.FileManager;

/**
 * Start screen: create a new project or open an existing one.
 *
 * @author Daniel Trageser
 */
public class StartingWindow {

  private static final String FILE_FORMAT = ".sji";
  private JFrame frame;

  public StartingWindow() {
    initialize();
  }

  /** Launch the application. */
  public static void main(String[] args) {
    EventQueue.invokeLater(
        () -> {
          FlatDarkLaf.setup();
          try {
            new StartingWindow().frame.setVisible(true);
          } catch (Exception e) {
            ErrorPopupWindow.throwMessage(e.getMessage());
          }
        });
  }

  private void initialize() {
    frame = new JFrame("SimplestJavaIDE Alpha v2.0");
    frame.setSize(520, 440);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setResizable(false);
    setWindowIcon();

    JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    JButton openButton = new JButton("Open existing code");
    openButton.setPreferredSize(new Dimension(200, 34));
    openButton.addActionListener(e -> openExisting());
    JButton newButton = new JButton("Code new application");
    newButton.setPreferredSize(new Dimension(200, 34));
    newButton.addActionListener(e -> createNew());
    buttonRow.add(openButton);
    buttonRow.add(newButton);
    frame.getContentPane().add(buttonRow, BorderLayout.NORTH);

    JPanel header = new JPanel(new BorderLayout());
    JLabel icon = new JLabel(scaledIcon());
    icon.setBorder(new EmptyBorder(10, 40, 10, 10));
    header.add(icon, BorderLayout.WEST);
    JLabel text =
        new JLabel(
            "<html><body style='text-align:center; margin:15px;'>"
                + "<h2>SimplestJavaIDE</h2><h4>Code in Java without classes!</h4></body></html>",
            SwingConstants.CENTER);
    header.add(text, BorderLayout.CENTER);

    JPanel center = new JPanel(new BorderLayout());
    center.add(header, BorderLayout.NORTH);
    center.add(buildRecentPanel(), BorderLayout.CENTER);
    frame.getContentPane().add(center, BorderLayout.CENTER);

    JButton helpButton = new JButton("Help");
    helpButton.addActionListener(e -> ImprintWindow.main(null));
    JPanel southRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
    southRow.add(helpButton);
    frame.getContentPane().add(southRow, BorderLayout.SOUTH);

    frame.getRootPane().setDefaultButton(newButton);
  }

  private void openExisting() {
    JFileChooser chooser = createChooser();
    if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    File file = chooser.getSelectedFile();
    String error = validationError(file);
    if (error != null) {
      ErrorPopupWindow.throwMessage(error);
      return;
    }
    if (!file.exists()) {
      ErrorPopupWindow.throwMessage("Chosen file does not exist. Please reselect.");
      return;
    }
    open(file, false);
  }

  private void createNew() {
    JFileChooser chooser = createChooser();
    if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    File selected = chooser.getSelectedFile();
    String error = validationError(selected);
    if (error != null) {
      ErrorPopupWindow.throwMessage(error);
      return;
    }
    File file =
        selected.toString().endsWith(FILE_FORMAT)
            ? selected
            : new File(selected + FILE_FORMAT);
    // FileManager creates the file when it does not exist yet.
    open(file, !file.exists());
  }

  /** Loads (or creates) the project file and opens the main window. */
  private void open(File file, boolean isNew) {
    String className = file.getName().replace(FILE_FORMAT, "");
    CodingFile codingFile = FileManager.load(className, file.getAbsolutePath(), isNew);
    if (codingFile != null) {
      AppPreferences.addRecentFile(file.getAbsolutePath());
      MainUserInput.launch(codingFile);
      frame.dispose();
    }
  }

  /** A list of buttons for recently opened files (empty when there are none). */
  private JPanel buildRecentPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(new EmptyBorder(0, 40, 10, 40));
    List<String> recent = AppPreferences.getRecentFiles();
    if (recent.isEmpty()) {
      return panel;
    }
    JLabel heading = new JLabel("Recently opened:");
    heading.setAlignmentX(0f);
    panel.add(heading);
    for (String path : recent) {
      File file = new File(path);
      JButton link = new JButton(file.getName());
      link.setToolTipText(path);
      link.setAlignmentX(0f);
      link.addActionListener(e -> open(file, false));
      panel.add(link);
    }
    return panel;
  }

  private JFileChooser createChooser() {
    JFileChooser chooser = new JFileChooser();
    chooser.setPreferredSize(new Dimension(800, 500));
    chooser.setSelectedFile(new File("ExampleProgram" + FILE_FORMAT));
    chooser.setFileFilter(new FileNameExtensionFilter("SimplestJavaIDE Files", "sji"));
    return chooser;
  }

  /** Returns an error message if the chosen file name is not a valid class name, else null. */
  private String validationError(File file) {
    if (file.getAbsolutePath().contains(" ")) {
      return "File path can't contain any spaces. Please reselect.";
    }
    String name = file.getName();
    if (!name.isEmpty() && Character.isDigit(name.charAt(0))) {
      return "File name can't start with a number. Please reselect.";
    }
    if (name.replace(FILE_FORMAT, "").contains(".")) {
      return "File name can't contain a period/dot. Please reselect.";
    }
    return null;
  }

  private ImageIcon scaledIcon() {
    try {
      return new ImageIcon(
          ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("favicon.png")))
              .getScaledInstance(128, 128, java.awt.Image.SCALE_SMOOTH));
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
      return new ImageIcon();
    }
  }

  private void setWindowIcon() {
    try {
      frame.setIconImage(
          ImageIO.read(
              Objects.requireNonNull(getClass().getClassLoader().getResource("favicon.png"))));
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }
  }
}
