# Spike: SimplestJavaIDE im Browser via CheerpJ (Variante A)

**Datum:** 2026-10-21 · **Getestet mit:** CheerpJ **4.3** (`https://cjrtnc.leaningtech.com/4.3/loader.js`),
eingebauter Browser (Chromium-Pane), lokal via `serve.py` (HTTP-Range-fähig).

## Kurzurteil

**Variante A (CheerpJ, rein statisch, kein Server) ist technisch machbar — aber nur mit
spürbarem Umbau und zwei harten Einschränkungen.** Die Swing-Oberfläche läuft im Browser
(bedienbar, FlatLaf-Theme intakt). Gebrochen sind genau die zwei Dinge, die der Task
vermutet hat: **das Ausführen von Schüler-Code als Separat-Prozess** und **der JDI-Debugger**.
Zusätzlich — und das ist der kritischste Befund — **bringt CheerpJs Laufzeit keinen
`javax.tools`-Compiler mit** (`getSystemJavaCompiler()` → `null`). Kompilieren im Browser geht
nur über einen **mitgelieferten reinen-Java-Compiler (ECJ)**, und der kompiliert unter CheerpJ
zuverlässig **nur auf Java-8-Sprachniveau**.

Entscheidung in einem Satz:
- **Priorität „ein Link, keine Installation, kein Server, kein Betrieb"** und Schüler-Java-8
  + kein (bzw. ersetzter) Debugger sind ok → **Variante A ist tragfähig**, mit dem unten
  skizzierten Umbau (ECJ-Compiler + In-JVM-Runner).
- **Debugger ist Pflicht** und/oder echtes `javac`/Java-21 mit minimalem App-Umbau gewünscht
  → **Variante B (Server)** empfehlen; dann aber Budget für sicheres Sandboxing von
  Schüler-Code einplanen.

---

## Messwerte & Antworten auf (a)–(e)

### (a) Läuft die Swing-Oberfläche im Browser?  → **JA**

- `StartingWindow` wird vollständig gerendert: FlatLaf-Dark-Theme, Logo/Icons, Buttons
  „Open…", „New project", „Help" (Screenshot im Spike dokumentiert).
- **Bedienbar:** Klick auf „New project" öffnet den „Save"-Dialog (JFileChooser) → Events,
  Fenster/Dialoge, Look-and-Feel funktionieren.
- Voraussetzung: Die App muss als **Java-17-Build** vorliegen (siehe (c)). Mit dem
  unveränderten Java-21-Jar startet die App **nicht**.

### (b) Funktioniert Kompilieren im Browser?  → **Nicht out-of-the-box; nur via ECJ auf Java-8-Niveau**

| Weg | Ergebnis |
|---|---|
| `ToolProvider.getSystemJavaCompiler()` (der Pfad der App heute) | **`null`** — CheerpJ liefert eine JRE **ohne `jdk.compiler`**. Der heutige Compile-Pfad der IDE funktioniert im Browser **nicht**. |
| ECJ (Eclipse Compiler, reines Java-Jar) über `javax.tools` `StandardFileManager` | **Exception** — ECJs Dateimanager öffnet beim Konstruieren das jrt-Modulimage `/lt/17/lib/jrt-fs.jar`, das CheerpJ **nicht** bereitstellt → `IOException`. |
| ECJ Batch-Compiler, `-source/-target 17`, `--system none` | **Fehler** — `invalid location for system libraries: none`. Ab Compliance 9+ verlangt ECJ ein echtes JDK-Modulimage. |
| ECJ Batch-Compiler, `-source/-target 17`, `-bootclasspath rt17.jar` | **Fehler** — `option -bootclasspath not supported at compliance level 9 and above`. |
| **ECJ Batch-Compiler, `-source/-target 1.8`, `-bootclasspath rt8.jar`** | **✅ ERFOLG** — kompiliert (`Hello8.class`, 764 B) **und** die frisch kompilierte Klasse wird im selben JVM per `URLClassLoader` geladen und ausgeführt (Ausgabe „Hallo (Java8)!"). Erste Kompilierung ≈ **4,8 s** (ECJ-Aufwärmen), danach schneller. |

**Fazit (b):** In-Browser-Kompilieren ist möglich, aber nur mit **gebündeltem ECJ** und
**Schüler-Sprachniveau Java 8** (klassischer Bootclasspath-Modus). Java-11/17-Sprachfeatures
für Schüler-Code sind mit dem aktuellen CheerpJ praktisch nicht machbar (kein jrt/`--system`-Image).
Für den Lernkontext ist Java 8 (Klassen, Methoden, Schleifen, Arrays, Collections, OOP)
völlig ausreichend — das generierte Gerüst der IDE ist ohnehin Java-8-kompatibel.

### (c) Welche Java-Version unterstützt CheerpJ?  → **Java 17** (kein 21)

Laufzeit laut Probe im Browser:
```
java.version           = 17.0.19-internal
java.specification.ver = 17
java.vm.name           = CheerpJ
java.vendor            = Leaning Technologies Ltd
java.home              = /lt/17/      os.arch = cheerpj
```
- **Java-21-Bytecode (major 65) lädt NICHT.** Das unveränderte `SimplestJavaIDE.jar` (Java 21)
  bleibt im Browser auf dem CheerpJ-Splash hängen und erreicht die Oberfläche nie (kein
  sauberer Fehler sichtbar).
- Ein **Java-17-Rebuild derselben Quellen läuft** — die genutzten modernen Features
  (records, text blocks, pattern-matching-instanceof) sind **alle ≤ Java 17**. Der Rebuild ist
  reiner Build-Flag (`-Dmaven.compiler.release=17`), **keine Quellcode-Änderung**.
- Java-21-Support steht auf CheerpJs Roadmap (LTS-Parität angepeilt ~Ende 2026), ist aber
  **noch nicht** ausgeliefert.

> Wichtig: Selbst wenn CheerpJ später Java 21 bringt, bleibt (b) bestehen — CheerpJ liefert
> eine **JRE ohne `jdk.compiler`**, also braucht man **in jedem Fall ECJ** zum Kompilieren.

### (d) Erst-Download & Zeiten — klassentauglich?  → **Ja, mit Caching**

- **App-Jar:** 3,6 MB; zum Anzeigen des Startfensters wurden via HTTP-Range nur **~2,34 MB**
  tatsächlich übertragen (CheerpJ lädt Jar-Teile bedarfsweise).
- **CheerpJ-Laufzeit:** wird vom CDN gestreamt und vom Browser **gecacht**; die genauen
  Cross-Origin-Größen sind messtechnisch verdeckt. CheerpJ-Doku/Erfahrungswerte: **~10–20 MB**
  Erstdownload für eine „durchschnittliche" App, danach aus dem Cache nahezu sofort.
- **Zeiten (warmer Cache, in dieser Session gemessen):** `cheerpjInit` wenige 10 ms,
  App bis sichtbares Fenster wenige Sekunden, ECJ-Trivialkompilat ~4,8 s (erstes Mal),
  Schüler-Programm-Lauf < 100 ms.
- **Klassentauglichkeit:** Erst-Besuch einmalig etwas langsamer (Laufzeit-Download), danach
  schnell. Für eine Schulklasse heißt das: beim ersten Öffnen pro Gerät/Browser Geduld,
  danach flott. Unkritisch, solange der Hoster vernünftige Caching-Header setzt.
- **Server-Anforderung:** Der statische Hoster **muss HTTP-Range unterstützen**. `python3 -m
  http.server` tut das **nicht** („HTTP server does not support the 'Range' header. CheerpJ
  cannot run.") — deshalb liegt hier `serve.py` als Range-fähiger Ersatz bei. GitHub Pages,
  Netlify, S3/CloudFront etc. unterstützen Range.

### (e) Was bricht — und was wäre der Umbau?

| Funktion heute | Im Browser | Umbau für Variante A |
|---|---|---|
| **Schüler-Code ausführen** via `ProcessBuilder` → `java -cp … Klasse` (`Runner`, `TerminalPanel.run()`) | **Unmöglich** — kein Prozess/`exec`, kein zweites JVM. | Im **selben JVM** per `URLClassLoader` laden & `main` aufrufen (im Spike bewiesen). Nötig: pro Lauf eigener ClassLoader; `System.out/err/in` pro Lauf umlenken (stdin aus dem Terminal-Panel einspeisen); `System.exit` abfangen (SecurityManager ist in 17 entfernt → Exit-Trap/eigener Mechanismus); Endlosschleifen: Lauf in Daemon-Thread + „Stop" über `Thread.interrupt()`/Watchdog (kein `process.destroy()` mehr). |
| **JDI-Schritt-Debugger** (`DebugSession` via `LaunchingConnector`, startet 2. JVM) | **Unmöglich** — kein zweites JVM, keine Attach-Sockets. | **Weglassen** oder ersetzen (z. B. Source-Level-Tracer/Bytecode-Instrumentierung — großer Aufwand) — oder den Debugger nur in **Variante B** serverseitig anbieten. |
| **Datei öffnen/speichern** (`FileManager`, JFileChooser) | Läuft, aber auf CheerpJs **virtuellem FS** (`/files`, persistiert in IndexedDB) statt echtem Dateisystem. | UX anpassen: Projekte im virtuellen FS; optional Export/Import als Download/Upload. `java.util.prefs` funktioniert (im Test initialisiert). |

---

## Empfehlung

1. **Wenn Variante A** (ein Link, kein Server): als nächsten Schritt einen **End-to-End-Folgespike**
   bauen, der (1) die App auf Java 17 baut, (2) `JavaCompilerService` auf **ECJ (source 8)** umstellt
   und (3) `Runner`/`TerminalPanel.run()` durch einen **In-JVM-ClassLoader-Runner** ersetzt — und
   das kombiniert im Browser testet (Schüler schreibt Code → kompiliert → läuft, Ausgabe im
   Terminal-Panel). Der Debugger entfällt zunächst. Erst danach groß investieren.
2. **Wenn der Debugger oder echtes javac/Java-21 Pflicht sind:** **Variante B (Server)** — App bleibt
   fast unverändert, aber sichere **Sandbox für Schüler-Code** ist zwingend (Ressourcenlimits,
   Zeitlimits, Isolation).
3. **Lizenz klären:** Die Konsole zeigt „CHEERPJ COMMUNITY EDITION — FOR PERSONAL AND NON-BUSINESS
   USE ONLY". Schulischer Einsatz braucht womöglich eine **kommerzielle/Bildungs-Lizenz** von
   Leaning Technologies — vor Investition abklären.

## Grenzen dieses Spikes

- Messwerte mit **warmem Browser-Cache**; echte „erste-Minute-pro-Gerät"-Zahlen brauchen einen
  Kaltstart-Test auf dem Zielhoster.
- Es wurde **kein App-Code geändert**; der Java-17-Build ist nur ein Build-Flag. Der In-JVM-Runner
  und die ECJ-Umstellung sind **skizziert und im Prinzip bewiesen** (Probe), aber **nicht** in die
  echte App integriert.
- ECJ-Java-8-Pfad braucht ein gebündeltes Plattform-Jar als Bootclasspath (hier `rt8.jar` aus
  JDK 8, 63 MB ungestrippt; CheerpJ streamt nur die benötigten Einträge, also kein 63-MB-Download —
  für Produktion sollte man es trotzdem auf die benötigten Pakete strippen).
