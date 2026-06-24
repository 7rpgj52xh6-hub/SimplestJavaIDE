# Projektdokumentation – SimplestJavaIDE

Diese Dokumentation beschreibt den Aufbau und die Funktionsweise der
SimplestJavaIDE für alle, die den Code verstehen, warten oder erweitern wollen.
Eine Bedienungsanleitung für Lernende steckt direkt in der App im
**Hilfe-/Über**-Fenster; das README erklärt Installation und Bau.

## Inhalt

1. [Ziel und Grundidee](#1-ziel-und-grundidee)
2. [Technischer Überblick](#2-technischer-überblick)
3. [Das Datenmodell](#3-das-datenmodell)
4. [Code-Generierung und Zeilen-Zuordnung](#4-code-generierung-und-zeilen-zuordnung)
5. [Kompilieren, Ausführen, Fehler-Zuordnung](#5-kompilieren-ausführen-fehler-zuordnung)
6. [Der Schritt-Debugger](#6-der-schritt-debugger)
7. [Klassen-Modus](#7-klassen-modus)
8. [Persistenz (.sji-Dateien)](#8-persistenz-sji-dateien)
9. [Editor-Komfort und Code-Vervollständigung](#9-editor-komfort-und-code-vervollständigung)
10. [Native Pakete und Laufzeit-Anforderungen](#10-native-pakete-und-laufzeit-anforderungen)
11. [Dateiübersicht](#11-dateiübersicht)
12. [Tests](#12-tests)

---

## 1. Ziel und Grundidee

Anfängerinnen und Anfänger sollen Java schreiben können, **bevor** sie Klassen,
Objekte, `static`, `String[] args` und Sichtbarkeiten verstehen. Deshalb zeigt
die IDE nur **Methoden** und **Imports**. Den ganzen Rahmen – die Klasse, in der
diese Methoden leben – erzeugt die IDE selbst.

Die `main`-Methode ist der Startpunkt. „Ausführen" speichert, generiert die
Klasse, kompiliert sie und startet das Programm in einem eigenen Prozess; die
Ein-/Ausgabe läuft über ein eingebautes Terminal.

## 2. Technischer Überblick

- **Sprache/Toolchain:** Java 21 (Bytecode), gebaut mit Maven. Build über JDK 24
  via `--release 21`.
- **GUI:** Java Swing mit [FlatLaf](https://www.formdev.com/flatlaf/) (dunkles
  Design) und [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea)
  als Editor, ergänzt um [AutoComplete](https://github.com/bobbylight/AutoComplete).
- **Kompilieren:** in-process über die Java-Compiler-API
  (`javax.tools.ToolProvider`) – kein externer `javac`-Aufruf.
- **Ausführen:** in einem **separaten Prozess** (`java -cp … Klasse`), damit eine
  Endlosschleife die IDE nicht einfriert und sauber abgebrochen werden kann.
- **Debugger:** Java Debug Interface (JDI, `com.sun.jdi`) – die IDE startet das
  Programm als Debuggee und steppt zeilenweise.
- **Persistenz:** JSON über [Gson](https://github.com/google/gson).

Der Aufbau der Fenster (von außen nach innen):

```
StartingWindow            Startbildschirm: neues / vorhandenes Projekt
  └─ MainUserInput        Hauptfenster, verdrahtet alle Panels + Toolbar-Aktionen
       ├─ ClassTabsPanel  Klassen-Tabs (im Standardmodus genau eine Klasse)
       │    └─ ClassEditor    Methoden-Tabs (+ optional ein „Klasse"-Tab)
       │         └─ CodingArea   ein Editor mit Highlighting, Completion, Live-Fehlern
       ├─ TerminalPanel   Toolbar (Run/Stop/Debug/Imports/Zoom/Klassen-Schalter)
       │                  + Terminal-Ausgabe + Eingabefeld/Senden
       ├─ ErrorListPanel  anklickbare Fehlerliste
       ├─ StatusBar       Statusmeldungen + Speicheranzeige
       └─ Debug/DebugPanel  Variablentabelle + Schritt-Steuerung
```

## 3. Das Datenmodell

Im Paket `Library.CodeStructure`:

- **`CodingFile`** – das ganze Projekt: `List<String> imports`,
  `List<JavaClass> classes`, `boolean expertMode` (Klassen-Modus) und der
  (transiente) Dateipfad. Enthält die gesamte Code-Generierung.
- **`JavaClass`** – eine Klasse: `name`, `List<Methods> methods` und `header`
  (der Klassenkopf inkl. Attribute, im Klassen-Modus editierbar). `hasMain()`
  prüft Kopf und Methoden auf eine `main`-Methode.
- **`Methods`** – eine Methode: Name und Quelltext.
- **`JavaNames`** – `isValidIdentifier` / `invalidReason` (mit deutschem Grund)
  und eine Menge reservierter Java-Schlüsselwörter. So lassen sich nur gültige
  Klassen- und Methodennamen vergeben.
- **`GeneratedProgram`** – das Ergebnis der Generierung (siehe nächster
  Abschnitt).

## 4. Code-Generierung und Zeilen-Zuordnung

Pro Klasse erzeugt `CodingFile.buildProgram()` **eine** `.java`-Datei nach dem
Schema:

```
<imports>
<Klassen-Shell …>
    <Methode 1 eingefügt vor der schließenden Klammer>
    <Methode 2 …>
}
```

Im Standardmodus ist die Shell schlicht `public class Name {\n}`; im
Klassen-Modus ist es der vom Nutzer geschriebene `header`. Die Methodenrümpfe
werden **vor die letzte `}`** injiziert.

Damit Fehler und Debug-Schritte wieder bei der richtigen Methode/Zeile landen,
merkt sich die Generierung für jede Methode einen **`MethodSpan`**
(`methodIndex`, `methodName`, `startLine`, `lineCount`). `GeneratedProgram`
bietet `locate(className, generatedLine)`, das eine generierte Zeile zurück auf
eine **`MethodLocation`** (`classIndex`, `methodIndex`, `methodName`,
`localLine`) abbildet. Der Klassenkopf hat `methodIndex == -1` (er gehört zum
„Klasse"-Tab, nicht zu einer Methode).

`writeSources()` schreibt jede Klasse als `<Klassenname>.java` in den
Projektordner (neben die `.sji`-Datei); `javaFilePaths()` / `generatedFilePaths()`
liefern die Pfade zum Kompilieren bzw. zum Aufräumen beim Schließen.

## 5. Kompilieren, Ausführen, Fehler-Zuordnung

Im Paket `Library.Commands`:

- **`JavaCompilerService`** kompiliert die generierten Dateien in-process über
  `ToolProvider.getSystemJavaCompiler()` und sammelt strukturierte
  `Diagnostic`s.
- Jeder Diagnostic wird über `GeneratedProgram.locate(...)` auf
  (Klasse, Methode, lokale Zeile) zurückgerechnet und
  - im Editor als **rote Schlängellinie** angezeigt (siehe `CodingArea` mit einem
    eigenen `Parser`),
  - in die **`ErrorListPanel`**-Liste eingetragen (Klick springt zur Stelle),
  - in der Konsole ausgegeben – ergänzt um eine verständliche Erklärung aus
    **`CompilerHints`** (deutsche Hinweise zu typischen Anfängerfehlern).
- **`Runner`** startet das fertige Programm als eigenen Prozess
  (`java.home/bin/java -cp <classpath> <Startklasse>`), liest stdout/stderr über
  **`StreamReader`** ins Terminal und kann es per `kill()` (inkl. Kindprozessen)
  beenden. So friert auch `while(true)` die IDE nicht ein.

Damit das Terminal bei viel Ausgabe nicht blockiert, wird die Ausgabe gepuffert
und gedrosselt auf dem Event-Dispatch-Thread eingespielt.

## 6. Der Schritt-Debugger

Im Paket `Library.Debug`:

- **`DebugSession`** startet das Programm über einen JDI-`LaunchingConnector`
  (`Bootstrap.virtualMachineManager().defaultConnector()`) als angehaltenen
  Debuggee mit demselben Laufzeit-JDK.
- Per `StepRequest` wird **Zeile für Zeile** vorgegangen. Nach jedem Schritt
  liest die Session aus dem aktuellen `StackFrame` die **lokalen Variablen und
  ihre Werte** aus und meldet einen **`TraceStep`** an die Oberfläche.
- **`DebugPanel`** zeigt die Variablentabelle, markiert die laufende Zeile und
  bietet *Weiter ▶*, *Bis Ende ▶▶* und *◀ Zurück* (schon ausgeführte Schritte
  erneut ansehen).
- Eingaben gibt man wie beim normalen Lauf ins Terminal-Eingabefeld; sie werden
  an den stdin des Debuggee weitergereicht.

Der Debugger ist bewusst **einfacher** als in großen IDEs (kein Breakpoint-
Gewusel), soll die Lernenden aber genau auf solche Werkzeuge vorbereiten.

## 7. Klassen-Modus

Standardmäßig **aus**. Der Schalter „Klassen" oben rechts (`expertMode` im
Modell) blendet die Klassen-Ebene ein:

- **`ClassTabsPanel`** zeigt oben Tabs je Klasse; die erste Klasse heißt wie das
  Projekt.
- Jede **`ClassEditor`** hat die Methoden-Tabs **plus** einen optisch
  abgesetzten **„Klasse"-Tab** (mit `{}`-Markersymbol), in dem `public class Name
  { … }` samt Attributen bearbeitet wird.
- Tab-Titel folgen immer dem Code: `CodingFile.declaredClassName(header)` bzw.
  `declaredMethodName(content)` lesen den tatsächlichen Namen aus dem Quelltext.
- Beim Generieren werden die Methodenrümpfe in den `header` der jeweiligen Klasse
  injiziert (siehe Abschnitt 4).
- Fälle ohne / mit mehreren `main`-Methoden behandelt `CodingFile`:
  `entryClass()` bevorzugt die erste Klasse mit `main`, `mainCount()` zählt sie;
  `TerminalPanel.ensureRunnable()` weist auf „keine `main`" (Lauf verhindert) bzw.
  „mehrere `main`" (Hinweis) hin.

## 8. Persistenz (.sji-Dateien)

`FileManager` (in `Library.CodeStructure`) speichert/lädt das `CodingFile` als
**JSON** über Gson; die Endung bleibt aus historischen Gründen `.sji`. Beim
Laden wird das JSON inspiziert: Enthält es noch das **alte** Schema (ohne
`classes`, dafür mit `imports`/`methods`/`javaClass`), wird es automatisch in das
neue Mehr-Klassen-Modell **migriert**. So öffnen sich Projekte aus früheren
Versionen weiterhin.

Gespeichert werden Imports, alle Klassen (mit Kopf und Methoden) und der
Klassen-Modus-Schalter. Autosave (alle 20 s) und manuelles Speichern (Strg+S)
nutzen denselben Pfad; eine Anzeige in der Statusleiste zeigt „Gespeichert" /
„Ungespeichert".

## 9. Editor-Komfort und Code-Vervollständigung

- **`CodingArea`** aktiviert automatische Einrückung, automatisches Schließen von
  `{ } ( ) [ ] " "` und Klammer-Hervorhebung und hängt einen Live-Fehler-`Parser`
  ein (debounced im Hintergrund kompiliert).
- **`CodeCompletion`** stellt einen gemeinsamen `DefaultCompletionProvider`
  bereit: kurze **Vorlagen** (`sout`, `soutv`, `fori`, `while`, `if`, `ifelse`,
  `main`, `scanner` – mit Cursor-/Platzhalterfeldern) und gängige **API-Namen**
  (`System.out.println()`, `nextInt()`, `Math.random()` …), jeweils mit deutschem
  Hinweistext. Auslösung automatisch beim Tippen oder per Strg+Leertaste.

## 10. Native Pakete und Laufzeit-Anforderungen

Die App **kompiliert und debuggt Java zur Laufzeit**. Das gebündelte Runtime
muss deshalb ein **vollständiges JDK** sein, kein abgespecktes JRE – konkret
werden gebraucht:

| Bedarf | Modul / Datei |
|--------|---------------|
| Schüler-Code kompilieren | `jdk.compiler` (`ToolProvider.getSystemJavaCompiler()`) |
| Debugger (JDI) | `jdk.jdi`, plus `jdk.jdwp.agent` im gestarteten Debuggee |
| Programm ausführen | `java.home/bin/java` (Launcher) |
| GUI | `java.desktop` |

Deshalb bündelt [`packaging/jpackage.sh`](../packaging/jpackage.sh) per
`--runtime-image "$JAVA_HOME"` das **komplette JDK** (Paketgröße ca. 100–250 MB).
`jpackage` kann nicht cross-kompilieren, daher wird pro Betriebssystem gebaut:

- **macOS:** `.dmg` (App in den Programme-Ordner ziehen).
- **Linux:** `.deb` für Debian/Ubuntu **und** ein portables `.tar.gz` (App-Image)
  für andere Distributionen.
- **Windows:** ein portables `.zip` (App-Image mit `SimplestJavaIDE.exe`). Für
  einen `.msi`/`.exe`-Installer müsste auf dem Runner das WiX-Toolset installiert
  werden.

Der Workflow [`.github/workflows/release.yml`](../.github/workflows/release.yml)
baut beim Pushen eines Tags `v*` automatisch **fünf** Varianten und hängt sie an
ein GitHub-Release: **macOS ARM64** (Apple Silicon – jeder Mac ab 2020), **Linux
x64 + ARM64** und **Windows x64 + ARM64** (inkl. *Windows on ARM* über den
`windows-11-arm`-Runner). Intel-Macs (`macos-x64`) sind bewusst nicht dabei, weil
GitHubs Intel-Mac-Runner stark verknappt sind; der Eintrag lässt sich bei Bedarf
in der Matrix wieder ergänzen.

## 11. Dateiübersicht

```
src/main/java/simplestJavaIDEpackage/
  StartingWindow.java        Einstieg: neues / vorhandenes Projekt
  MainUserInput.java         Hauptfenster, verdrahtet Panels + Aktionen, Autosave
  Theme.java                 Farben des dunklen Designs
  Icons.java                 vektorbasierte Toolbar-Icons
  Notifications.java         kurze Statusmeldungen
  StatusBar.java             Statusleiste + Speicheranzeige
  ErrorListPanel.java        anklickbare Fehlerliste
  ErrorPopupWindow.java      Fehlerdialog
  ImprintWindow.java         Hilfe-/Über-Fenster
  AppPreferences.java        gemerkte Einstellungen (zuletzt geöffnet, Schrift …)
  Library/
    ClassTabsPanel.java      Klassen-Tabs (beide Modi)
    ClassEditor.java         Methoden-Tabs (+ optionaler „Klasse"-Tab)
    CodingArea.java          ein Editor: Highlighting, Completion, Live-Fehler
    CodeCompletion.java      Vorlagen + API-Vorschläge
    TerminalPanel.java       Toolbar + Terminal, treibt Kompilieren/Lauf/Debug
    AddImportsWindow.java    Imports verwalten
    CodeStructure/
      CodingFile.java        Projektmodell + Code-Generierung
      JavaClass.java         eine Klasse (Kopf + Methoden)
      Methods.java           eine Methode (Name + Quelltext)
      JavaNames.java         Validierung von Java-Bezeichnern
      GeneratedProgram.java  generierter Code + Zeilen-Zuordnung
      FileManager.java       JSON-Laden/-Speichern (+ Migration alter Dateien)
    Commands/
      JavaCompilerService.java  in-process Kompilierung + Diagnostics
      CompilerHints.java        deutsche Erklärungen zu Compiler-Fehlern
      Runner.java               führt das Programm als eigenen Prozess aus
      StreamReader.java         liest stdout/stderr ins Terminal
      CommandListener.java      Callback-Schnittstelle
    Debug/
      DebugSession.java      JDI-Sitzung: startet & steppt das Programm
      DebugPanel.java        Variablentabelle + Schritt-Steuerung
      TraceStep.java         ein Debug-Schritt (Zeile + Variablenwerte)

packaging/
  jpackage.sh                baut das native Paket der aktuellen Plattform
  icons/                     App-Icons (.icns / .ico / .png)

.github/workflows/
  build.yml                  CI: baut & testet das Jar bei jedem Push
  release.yml                baut native Pakete für alle Plattformen bei Tag v*
```

## 12. Tests

`src/test/java/.../CodeGenerationTest.java` (JUnit) prüft u. a.:

- die Zeilen-Zuordnung generierter Code → Methode (inkl. Klassenkopf =
  `methodIndex −1`),
- die Zuordnung von Compiler-Fehlern,
- die Validierung von Java-Namen (`JavaNames`),
- die Migration alter `.sji`-Dateien mit Einzelklassen-Schema.

```bash
mvn test
```
