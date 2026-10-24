// A small help / about overlay (German), mirroring the desktop app's help window.

const HTML = `
  <div class="modal">
    <h2>SimplestJavaIDE</h2>
    <p>Java schreiben und ausführen – ohne dich um Klassen oder Drumherum zu kümmern.
       Du schreibst nur Methoden; <b>main</b> ist der Startpunkt.</p>

    <h3>Erste Schritte</h3>
    <ul>
      <li><b>Run (▶)</b> kompiliert und startet dein Programm. Die Ausgabe erscheint in der Konsole.</li>
      <li>Erwartet dein Programm eine Eingabe (<code>Scanner</code>), tippst du sie ins
          <i>Input</i>-Feld und drückst <b>Senden</b> (oder Enter).</li>
      <li><b>Stop (■)</b> beendet ein laufendes Programm (z. B. eine Endlosschleife).</li>
      <li>Fehler werden <b>rot</b> markiert – mit der Zeile und einem Tipp in einfacher Sprache.</li>
      <li><b>Strg/Cmd + Leertaste</b> öffnet die Code-Vervollständigung.</li>
    </ul>

    <h3>Tastenkürzel</h3>
    <ul>
      <li><b>Strg/Cmd + S</b> – Speichern</li>
      <li><b>Strg/Cmd + Enter</b> oder <b>F5</b> – Ausführen</li>
      <li><b>Strg/Cmd + Leertaste</b> – Vorschläge</li>
    </ul>

    <p class="muted">Läuft komplett im Browser – dein Code bleibt auf deinem Gerät.
       Für den Informatik-/IT-Unterricht an deutschen Berufsschulen.</p>
    <div class="modal-actions"><button id="help-close" class="tool-btn">Schließen</button></div>
  </div>
`;

export function showHelp(): void {
  const open = document.getElementById("help-overlay");
  if (open) { open.remove(); return; }
  const overlay = document.createElement("div");
  overlay.id = "help-overlay";
  overlay.className = "modal-overlay";
  overlay.innerHTML = HTML;
  overlay.addEventListener("click", (e) => { if (e.target === overlay) overlay.remove(); });
  document.addEventListener("keydown", function esc(e) {
    if (e.key === "Escape") { overlay.remove(); document.removeEventListener("keydown", esc); }
  });
  document.body.appendChild(overlay);
  overlay.querySelector<HTMLButtonElement>("#help-close")!.addEventListener("click", () => overlay.remove());
}
