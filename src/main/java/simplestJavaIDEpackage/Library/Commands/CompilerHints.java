package simplestJavaIDEpackage.Library.Commands;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Turns cryptic compiler messages into plain-language hints for beginners. The
 * hints are in German, matching the target audience (German vocational schools);
 * the original compiler message is still shown next to them.
 *
 * @author Daniel Trageser
 */
public final class CompilerHints {

  private CompilerHints() {}

  /** Returns a beginner-friendly explanation for a diagnostic, or null if none. */
  public static String friendlyHint(Diagnostic<? extends JavaFileObject> diagnostic) {
    String code = diagnostic.getCode();
    if (code == null) {
      return null;
    }
    // "cannot find symbol" family (typo, missing declaration, missing import).
    if (code.startsWith("compiler.err.cant.resolve")) {
      return "Ein Name ist unbekannt. Tippfehler? Variable nicht deklariert? "
          + "Oder fehlt ein import?";
    }
    if (code.startsWith("compiler.err.cant.apply")) {
      return "Die Methode wird mit den falschen Argumenten aufgerufen "
          + "(Anzahl oder Datentypen passen nicht).";
    }
    if (code.startsWith("compiler.err.non-static")) {
      return "In diesem Programm sind alle Methoden 'static'. Rufe nur "
          + "static-Methoden auf oder mache die Variable 'static'.";
    }
    return switch (code) {
      case "compiler.err.expected" ->
          "Hier fehlt ein Zeichen — meistens ein Semikolon ; am Zeilenende "
              + "oder eine schließende Klammer ) bzw. }.";
      case "compiler.err.illegal.start.of.expr", "compiler.err.illegal.start.of.stmt" ->
          "Etwas Unerwartetes — oft eine Klammer { } zu viel oder zu wenig, "
              + "oder ein vergessenes Semikolon in der Zeile davor.";
      case "compiler.err.not.stmt" ->
          "Diese Zeile ist keine gültige Anweisung. Fehlt eine Zuweisung (=) "
              + "oder ein Methodenaufruf?";
      case "compiler.err.prob.found.req" ->
          "Die Datentypen passen nicht zusammen — z. B. Text einer Zahl-Variable "
              + "zugewiesen. Prüfe die Typen.";
      case "compiler.err.missing.ret.stmt" ->
          "Die Methode muss noch einen Wert zurückgeben (return …).";
      case "compiler.err.unreported.exception.need.to.catch.or.throw" ->
          "Dieser Code kann einen Fehler auslösen, den du behandeln musst "
              + "(try/catch oder throws).";
      case "compiler.err.already.defined" ->
          "Diesen Namen gibt es in diesem Bereich schon — wähle einen anderen.";
      case "compiler.err.var.might.not.have.been.initialized" ->
          "Die Variable wird benutzt, bevor ihr ein Wert zugewiesen wurde.";
      case "compiler.err.unclosed.str.lit" ->
          "Ein Text (String) wurde nicht mit \" geschlossen.";
      case "compiler.err.unclosed.char.lit" ->
          "Ein Zeichen wurde nicht mit ' geschlossen.";
      case "compiler.err.doesnt.exist" ->
          "Dieses Paket gibt es nicht — prüfe den import auf Tippfehler.";
      case "compiler.err.premature.eof" ->
          "Das Programm endet zu früh — vermutlich fehlt eine schließende } Klammer.";
      default -> null;
    };
  }
}
