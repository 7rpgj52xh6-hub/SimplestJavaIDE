## SimplestJavaIDE

Eine minimalistische Java-IDE für den Unterricht: Java schreiben und ausführen,
ohne sich um Klassen oder Boilerplate kümmern zu müssen.

**Du brauchst kein installiertes Java** – in jedem Paket steckt schon ein
komplettes JDK.

### Download

| Plattform | Datei | Installation |
|-----------|-------|--------------|
| **macOS** (Apple Silicon) | `…-macos-arm64.dmg` | Öffnen, App in den Programme-Ordner ziehen |
| **Windows** (x64) | `…-windows-x64.zip` | Entpacken, `SimplestJavaIDE.exe` starten |
| **Windows on ARM** | `…-windows-arm64.zip` | Entpacken, `SimplestJavaIDE.exe` starten |
| **Linux** (x64 / ARM64) | `…-linux-*.deb` | `sudo apt install ./…​.deb` |
| **Linux** (andere Distributionen) | `…-linux-*.tar.gz` | Entpacken, `SimplestJavaIDE/bin/SimplestJavaIDE` starten |

### Erster Start

Die Pakete sind (noch) nicht signiert, daher meldet sich beim ersten Start ggf.
das Betriebssystem:

- **macOS:** Rechtsklick auf die App → *Öffnen* → *Öffnen*.
- **Windows:** Bei „Der Computer wurde geschützt" auf *Weitere Informationen* →
  *Trotzdem ausführen*.

### Was ist drin

- Tab-Editor mit Syntaxhervorhebung, Live-Fehlerprüfung und anklickbarer
  Fehlerliste (Fehler werden auf die richtige Methode/Zeile zurückgerechnet).
- Kompilieren & Ausführen auf Knopfdruck mit interaktivem Terminal und Stop-Knopf.
- Schritt-Debugger mit Variablenanzeige.
- Code-Vervollständigung mit deutschen Hinweisen, Imports-Dialog, Klassen-Modus
  (zuschaltbar), Autosave und gemerkte zuletzt geöffnete Dateien.
