package simplestJavaIDEpackage.Library;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.TemplateCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Beginner-friendly code completion: a few short templates (sout, fori, scanner …)
 * and common API names. Triggered automatically while typing and with Ctrl+Space.
 *
 * @author Daniel Trageser
 */
public final class CodeCompletion {

  private static final CompletionProvider PROVIDER = createProvider();

  private CodeCompletion() {}

  /** Installs auto-completion on the given editor (using a shared provider). */
  public static void install(RSyntaxTextArea textArea) {
    AutoCompletion ac = new AutoCompletion(PROVIDER);
    ac.setAutoActivationEnabled(true);
    ac.setAutoActivationDelay(350);
    ac.setShowDescWindow(true);
    ac.setAutoCompleteSingleChoices(false);
    ac.install(textArea);
  }

  private static CompletionProvider createProvider() {
    DefaultCompletionProvider provider = new DefaultCompletionProvider();
    provider.setAutoActivationRules(true, null); // pop up after letters

    // Templates (${cursor} = where the caret lands, ${name} = a fill-in field).
    template(provider, "sout", "Text ausgeben", "System.out.println(${cursor});");
    template(provider, "soutv", "Variable ausgeben", "System.out.println(${var});");
    template(provider, "fori", "Zählschleife", "for (int ${i} = 0; ${i} < ${n}; ${i}++) {\n\t${cursor}\n}");
    template(provider, "while", "while-Schleife", "while (${bedingung}) {\n\t${cursor}\n}");
    template(provider, "if", "if-Verzweigung", "if (${bedingung}) {\n\t${cursor}\n}");
    template(provider, "ifelse", "if-else-Verzweigung", "if (${bedingung}) {\n\t${cursor}\n} else {\n\t\n}");
    template(provider, "main", "Hauptmethode", "public static void main(String[] args) {\n\t${cursor}\n}");
    template(provider, "scanner", "Eingabe einlesen", "Scanner ${sc} = new Scanner(System.in);\n${cursor}");

    // Common API names.
    basic(provider, "System.out.println()", "Zeile ausgeben");
    basic(provider, "System.out.print()", "ohne Zeilenumbruch ausgeben");
    basic(provider, "nextInt()", "ganze Zahl einlesen");
    basic(provider, "nextLine()", "Zeile einlesen");
    basic(provider, "nextDouble()", "Kommazahl einlesen");
    basic(provider, "Integer.parseInt()", "Text in int umwandeln");
    basic(provider, "Double.parseDouble()", "Text in double umwandeln");
    basic(provider, "Math.random()", "Zufallszahl 0..1");
    basic(provider, "Math.max()", "größere Zahl");
    basic(provider, "Math.min()", "kleinere Zahl");
    basic(provider, "length()", "Länge");
    basic(provider, "charAt()", "Zeichen an Position");
    basic(provider, "equals()", "Inhalt vergleichen");
    basic(provider, "toUpperCase()", "in Großbuchstaben");
    basic(provider, "toLowerCase()", "in Kleinbuchstaben");
    return provider;
  }

  private static void template(
      DefaultCompletionProvider provider, String input, String desc, String template) {
    provider.addCompletion(new TemplateCompletion(provider, input, desc, template));
  }

  private static void basic(DefaultCompletionProvider provider, String text, String desc) {
    provider.addCompletion(new BasicCompletion(provider, text, desc));
  }
}
