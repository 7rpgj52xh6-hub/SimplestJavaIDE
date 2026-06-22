package simplestJavaIDEpackage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

/**
 * Concise help &amp; about window: how to use the IDE, keyboard shortcuts, and
 * credits. (Previously this was a wall of license text.)
 *
 * @author Daniel Trageser
 */
public class ImprintWindow {

  private static final String HELP_HTML =
      """
      <html>
      <body style="font-family: sans-serif; margin: 16px;">
        <h2>SimplestJavaIDE</h2>
        <p>Write and run Java without dealing with classes or boilerplate &ndash;
           you only edit method bodies, each in its own tab.</p>

        <h3>Getting started</h3>
        <ul>
          <li>Write your code in the method tabs. The <b>Main Method</b> is the entry point.</li>
          <li>Click <b>+</b> to add a method; <b>right-click a tab</b> to rename or delete it.</li>
          <li>Press <b>Run</b> to compile and run. Program input goes in the <i>Input</i> line.</li>
          <li>If there is a compile error, the IDE jumps to the method and line and
              highlights it.</li>
          <li>Use <b>Add imports</b> to manage your imports.</li>
        </ul>

        <h3>Keyboard shortcuts</h3>
        <ul>
          <li><b>Ctrl/Cmd + S</b> &ndash; save</li>
          <li><b>F5</b> or <b>Ctrl/Cmd + R</b> &ndash; run</li>
          <li><b>Ctrl/Cmd + Plus / Minus</b> &ndash; larger / smaller font</li>
        </ul>

        <h3>Syntax-Spickzettel</h3>
        <pre>
        // Ausgabe
        System.out.println("Hallo");

        // Variablen
        int zahl = 5;
        double komma = 3.14;
        String text = "Hi";
        boolean ja = true;

        // Bedingung
        if (zahl &gt; 3) {
            System.out.println("groß");
        } else {
            System.out.println("klein");
        }

        // Schleifen
        for (int i = 0; i &lt; 5; i++) {
            System.out.println(i);
        }
        while (zahl &gt; 0) {
            zahl = zahl - 1;
        }

        // Eingabe (Scanner) — import java.util.* nicht vergessen!
        Scanner scanner = new Scanner(System.in);
        int eingabe = scanner.nextInt();
        </pre>

        <h3>About</h3>
        <p>&copy; Daniel Trageser. Designed for teaching Java in German vocational
           schools.<br>
           Contact: daniel.trageser@outlook.com</p>
        <p style="color: gray;">Built with RSyntaxTextArea (BSD), FlatLaf (Apache 2.0)
           and Gson (Apache 2.0).</p>
      </body>
      </html>
      """;

  private JFrame frame;

  public ImprintWindow() {
    initialize();
  }

  /** Launch the window. */
  public static void main(String[] args) {
    EventQueue.invokeLater(
        () -> {
          try {
            new ImprintWindow().frame.setVisible(true);
          } catch (Exception e) {
            ErrorPopupWindow.throwMessage(e.getMessage());
          }
        });
  }

  private void initialize() {
    frame = new JFrame("SimplestJavaIDE - Help & About");
    frame.setSize(560, 560);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    try {
      frame.setIconImage(
          ImageIO.read(
              Objects.requireNonNull(getClass().getClassLoader().getResource("favicon.png"))));
    } catch (IOException e) {
      ErrorPopupWindow.throwMessage(e.getMessage());
    }

    JTextPane content = new JTextPane();
    content.setContentType("text/html");
    content.setEditable(false);
    content.setText(HELP_HTML);
    content.setCaretPosition(0);
    frame.getContentPane().add(new JScrollPane(content), BorderLayout.CENTER);

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> frame.dispose());
    JPanel buttonRow = new JPanel(new BorderLayout());
    buttonRow.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    buttonRow.add(closeButton, BorderLayout.EAST);
    frame.getContentPane().add(buttonRow, BorderLayout.SOUTH);
    frame.getRootPane().setDefaultButton(closeButton);
  }
}
