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
        <p>Java schreiben und ausführen, ohne dich um Klassen oder Drumherum zu
           kümmern &ndash; du bearbeitest nur Methoden, jede in ihrem eigenen Tab.</p>

        <h3>Erste Schritte</h3>
        <ul>
          <li>Schreib deinen Code in die Methoden-Tabs. Die <b>Main Method</b> ist
              der Startpunkt &ndash; hier beginnt dein Programm.</li>
          <li>Mit <b>+</b> legst du eine neue Methode an; per <b>Rechtsklick auf einen
              Tab</b> kannst du sie umbenennen oder löschen.</li>
          <li><b>Run</b> speichert, kompiliert und führt dein Programm aus. Die
              Ausgabe erscheint unten in der Konsole.</li>
          <li>Erwartet dein Programm eine Eingabe, tippst du sie ins <i>Input</i>-Feld
              und klickst <b>Senden</b> (oder drückst Enter).</li>
          <li>Bei einem Fehler springt die IDE zur Methode und Zeile und markiert sie
              rot &ndash; mit einem Tipp in einfacher Sprache.</li>
          <li><b>Stop</b> beendet ein laufendes Programm (z. B. eine Endlosschleife).</li>
          <li>Über <b>Imports</b> fügst du fertige Bausteine hinzu (z. B. Scanner).</li>
        </ul>

        <h3>Debugger &ndash; Schritt für Schritt</h3>
        <ul>
          <li>Mit <b>Debug</b> startest du dein Programm angehalten.</li>
          <li><b>Weiter ▶</b> führt genau eine Zeile aus. Rechts siehst du dabei alle
              Variablen und ihre <b>aktuellen Werte</b>, die laufende Zeile wird grün
              markiert.</li>
          <li>Eingaben gibst du ganz normal ins <i>Input</i>-Feld ein, während du
              steppst.</li>
          <li><b>Bis Ende ▶▶</b> lässt den Rest durchlaufen, <b>◀ Zurück</b> zeigt
              schon ausgeführte Schritte noch einmal an.</li>
        </ul>

        <h3>Tastenkürzel</h3>
        <ul>
          <li><b>Strg/Cmd + S</b> &ndash; speichern</li>
          <li><b>F5</b> oder <b>Strg/Cmd + R</b> &ndash; ausführen</li>
          <li><b>Strg/Cmd + Plus / Minus</b> &ndash; Schrift größer / kleiner</li>
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

        // Eingabe (Scanner) — import java.util.Scanner; nicht vergessen!
        Scanner scanner = new Scanner(System.in);
        int eingabe = scanner.nextInt();
        </pre>

        <h3>Über</h3>
        <p>&copy; Daniel Trageser. Für den Java-Unterricht an deutschen Berufsschulen.<br>
           Kontakt: daniel.trageser@outlook.com</p>
        <p style="color: gray;">Gebaut mit RSyntaxTextArea (BSD), FlatLaf (Apache 2.0)
           und Gson (Apache 2.0).</p>
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
