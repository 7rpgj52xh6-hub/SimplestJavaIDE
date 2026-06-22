package simplestJavaIDEpackage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import simplestJavaIDEpackage.Library.CodeStructure.CodingFile;
import simplestJavaIDEpackage.Library.CodeStructure.FileManager;

/**
 * Start screen: create a new project or open an existing / recent one.
 *
 * @author Daniel Trageser
 */
public class StartingWindow {

  private static final String FILE_FORMAT = ".sji";
  private static final Color MUTED = new Color(0x9AA0A6);
  private JFrame frame;

  public StartingWindow() {
    initialize();
  }

  /** Launch the application. */
  public static void main(String[] args) {
    EventQueue.invokeLater(
        () -> {
          Theme.setup();
          try {
            new StartingWindow().frame.setVisible(true);
          } catch (Exception e) {
            ErrorPopupWindow.throwMessage(e.getMessage());
          }
        });
  }

  private void initialize() {
    frame = new JFrame("SimplestJavaIDE");
    frame.setSize(560, 500);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setResizable(false);
    setWindowIcon();

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.setBorder(new EmptyBorder(30, 36, 16, 36));

    JLabel logo = new JLabel(scaledIcon(96));
    logo.setAlignmentX(Component.CENTER_ALIGNMENT);
    content.add(logo);

    JLabel title = new JLabel("SimplestJavaIDE");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
    title.setAlignmentX(Component.CENTER_ALIGNMENT);
    title.setBorder(new EmptyBorder(14, 0, 0, 0));
    content.add(title);

    JLabel tagline = new JLabel("Code in Java without classes");
    tagline.setForeground(MUTED);
    tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
    tagline.setBorder(new EmptyBorder(4, 0, 22, 0));
    content.add(tagline);

    content.add(buildButtonRow());

    JPanel recent = buildRecentPanel();
    recent.setAlignmentX(Component.CENTER_ALIGNMENT);
    content.add(recent);

    frame.getContentPane().add(content, BorderLayout.CENTER);

    JButton helpButton = new JButton("Help");
    helpButton.setFocusable(false);
    helpButton.addActionListener(e -> ImprintWindow.main(null));
    JPanel southRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
    southRow.add(helpButton);
    frame.getContentPane().add(southRow, BorderLayout.SOUTH);
  }

  private JPanel buildButtonRow() {
    JButton openButton = new JButton("Open…");
    openButton.setFocusable(false);
    openButton.setPreferredSize(new Dimension(150, 40));
    openButton.addActionListener(e -> openExisting());

    JButton newButton = new JButton("New project");
    newButton.setFocusable(false);
    newButton.setPreferredSize(new Dimension(150, 40));
    newButton.putClientProperty("JButton.buttonType", "default"); // accent primary
    newButton.addActionListener(e -> createNew());

    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
    row.setAlignmentX(Component.CENTER_ALIGNMENT);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
    row.add(openButton);
    row.add(newButton);
    frame.getRootPane().setDefaultButton(newButton);
    return row;
  }

  /** A list of buttons for recently opened files (empty when there are none). */
  private JPanel buildRecentPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(new EmptyBorder(24, 0, 0, 0));
    List<String> recent = AppPreferences.getRecentFiles();
    if (recent.isEmpty()) {
      return panel;
    }
    JLabel heading = new JLabel("Recently opened");
    heading.setForeground(MUTED);
    heading.setAlignmentX(Component.CENTER_ALIGNMENT);
    heading.setBorder(new EmptyBorder(0, 0, 8, 0));
    panel.add(heading);
    for (String path : recent) {
      File file = new File(path);
      JButton link = new JButton(file.getName());
      link.setToolTipText(path);
      link.setFocusable(false);
      link.setAlignmentX(Component.CENTER_ALIGNMENT);
      link.setMaximumSize(new Dimension(280, 30));
      link.addActionListener(e -> open(file, false));
      panel.add(link);
      panel.add(Box.createVerticalStrut(5));
    }
    return panel;
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
        selected.toString().endsWith(FILE_FORMAT) ? selected : new File(selected + FILE_FORMAT);
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

  private ImageIcon scaledIcon(int size) {
    try {
      return new ImageIcon(
          ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResource("favicon.png")))
              .getScaledInstance(size, size, Image.SCALE_SMOOTH));
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
