# SimplestJavaIDE

Eine minimalistische Java-IDE für den **Unterricht**, gebaut mit Java Swing.
Ziel: Schülerinnen und Schüler sollen **Java schreiben und ausführen, ohne sich
um Klassen, Objekte oder Boilerplate kümmern zu müssen**. Entstanden für den
Informatik-/IT-Unterricht an deutschen Berufsschulen (z. B. Fachoberschule
Informationstechnik, 11. Klasse).

> Version **Alpha 2.0**

---

## Die Idee

Ein Programm ist hier einfach eine Sammlung von **Methoden** plus einer Liste
von **Imports**. Die IDE versteckt alles andere: Im Hintergrund packt sie die
Methoden in eine generierte Klasse, kompiliert sie und führt sie aus – die
Lernenden sehen und bearbeiten nur die Methodenrümpfe, jede in einem eigenen
Tab. Die `main`-Methode ist der Startpunkt.

Fortgeschrittene können später den **Klassen-Modus** zuschalten (ein Schalter
oben rechts, standardmäßig aus) und dann mit echten Klassen, Attributen und
mehreren Klassen-Tabs arbeiten – als sanfter Übergang zur „echten" Java-Welt.

---

## Installation (fertige Programme)

Du brauchst **kein** installiertes Java – in jedem Paket steckt schon ein
komplettes JDK. Lade dir unter **[Releases](../../releases)** das passende Paket
herunter:

| Plattform | Datei | Installation |
|-----------|-------|--------------|
| **macOS** (Apple Silicon / Intel) | `…-macos-arm64.dmg` / `…-macos-x64.dmg` | Öffnen, App in den Programme-Ordner ziehen |
| **Windows** (x64 / ARM64) | `…-windows-x64.zip` / `…-windows-arm64.zip` | Entpacken, `SimplestJavaIDE.exe` starten |
| **Linux** (x64 / ARM64) | `…-linux-x64.deb` / `…-linux-arm64.deb` | `sudo apt install ./…​.deb` |
| **Linux** (andere Distributionen) | `…-linux-x64.tar.gz` | Entpacken, `SimplestJavaIDE/bin/SimplestJavaIDE` starten |

> **Windows on ARM:** Ja, wird unterstützt – es gibt ein eigenes
> `windows-arm64`-Paket (z. B. für Surface-Pro-Geräte mit ARM-Prozessor).

Auf macOS meldet sich beim ersten Start ggf. Gatekeeper, weil die App nicht
signiert ist: Rechtsklick → *Öffnen* → *Öffnen*.

---

## Funktionen

- **Tab-Editor** mit Java-Syntaxhervorhebung (RSyntaxTextArea) und dunklem
  Design (FlatLaf).
- **Kompilieren & Ausführen** auf Knopfdruck, mit interaktivem Terminal
  (Ein-/Ausgabe) und **Stop**-Knopf, der auch Endlosschleifen sicher beendet.
- **Fehler werden auf die richtige Methode und Zeile zurückgerechnet** – nicht
  auf die versteckte generierte Datei. Syntaxfehler erscheinen **live rot** im
  Editor, dazu eine **anklickbare Fehlerliste**, die direkt zur Stelle springt.
  Compiler-Meldungen werden in einfacher Sprache erklärt.
- **Code-Vervollständigung** für Anfänger: kurze Vorlagen (`sout`, `fori`,
  `scanner`, `if`, `while`, `main` …) und gängige API-Namen, jeweils mit
  deutschem Hinweis (Strg+Leertaste oder automatisch beim Tippen).
- **Schritt-Debugger**: Programm angehalten starten, Zeile für Zeile vorgehen,
  dabei rechts alle **Variablen und ihre aktuellen Werte** sehen – einfacher als
  in „großen" IDEs, aber als Vorbereitung darauf gedacht.
- **Imports-Dialog**, in dem komplette `import`-Zeilen eingefügt werden (mit
  Schnell-Knöpfen z. B. für `Scanner`), damit die Lernenden sehen, wie eine
  echte Import-Zeile aussieht.
- **Klassen-Modus** (zuschaltbar): Klassen-Tabs oben, je Klasse eigene
  Methoden-Tabs plus ein „Klasse"-Tab für Attribute; Umgang mit keiner / einer /
  mehreren `main`-Methoden.
- **Komfort**: automatische Einrückung, automatisches Schließen von Klammern und
  Anführungszeichen, Klammer-Hervorhebung.
- **Autosave** alle 20 Sekunden, Speicheranzeige in der Statusleiste,
  Tastenkürzel (Speichern, Ausführen, Zoom), zuletzt geöffnete Dateien – alles
  wird zwischen Sitzungen gemerkt.

Ein Projekt wird in einer **`.sji`-Datei (JSON)** gespeichert: Imports, Klassen
mit ihren Methoden und der Klassenkopf. Alte `.sji`-Dateien aus früheren
Versionen werden beim Öffnen automatisch migriert.

---

## Aus dem Quellcode bauen

Voraussetzung: ein **JDK 21 oder neuer** (die IDE kompiliert den Schüler-Code
zur Laufzeit über die Compiler-API, ein reines JRE genügt also nicht).

```bash
mvn clean package
```

Das erzeugt ein einzelnes ausführbares Jar mit allen Abhängigkeiten unter
`target/SimplestJavaIDE.jar`:

```bash
java -jar target/SimplestJavaIDE.jar
```

### Native Pakete selbst bauen

Für die plattformeigenen Pakete (mit gebündeltem JDK) gibt es das Skript
[`packaging/jpackage.sh`](packaging/jpackage.sh). `jpackage` kann **nicht**
cross-kompilieren – führe das Skript also auf dem jeweiligen Betriebssystem aus:

```bash
mvn -B clean package        # zuerst das Jar bauen
packaging/jpackage.sh dmg   # macOS: .dmg   | Linux: deb/app-image | Windows: app-image
```

Im CI übernimmt das [`.github/workflows/release.yml`](.github/workflows/release.yml)
automatisch für alle sechs Ziele (macOS/Linux/Windows × x64/ARM64), sobald ein
Tag wie `v2.0` gepusht wird.

Mehr Hintergründe und der Aufbau des Projekts stehen in der
**[Projektdokumentation](docs/Projektdokumentation.md)**.

---

## Tests

```bash
mvn test
```

Decken die Zeilen-Zuordnung (generierter Code → Methode), die
Compiler-Fehler-Zuordnung, die Namensvalidierung und das JSON-Speichern ab.

---

## Lizenz / Credits

© Daniel Trageser. Für den Java-Unterricht an deutschen Berufsschulen.
Gebaut mit RSyntaxTextArea (BSD), FlatLaf (Apache 2.0), AutoComplete (BSD) und
Gson (Apache 2.0) – Lizenztexte siehe der Hilfe-/Über-Dialog in der App.
