# CheerpJ-Spike — Test-Harness

Prüft, ob die **SimplestJavaIDE** via **CheerpJ** (WebAssembly-JVM, statisch, kein Server)
im Browser läuft. Ergebnis & Urteil: **[FINDINGS.md](FINDINGS.md)**.

## Dateien

| Datei | Zweck |
|---|---|
| `index.html` | lädt CheerpJ 4.3 und startet den **Java-17-Build** der IDE (Frage a, d) |
| `app-java21.html` | startet den **Java-21-Build** → bleibt hängen (Beleg für Frage c) |
| `probe.html` | `probe.CompilerProbe`: Laufzeit-Infos + `getSystemJavaCompiler()` (Frage b, c) |
| `ecj-probe.html` | ECJ via `javax.tools` (scheitert am fehlenden jrt) |
| `ecj-batch-probe.html` | `probe.EcjBatchProbe`: ECJ-Batch mit mehreren Strategien → Java-8-Weg klappt (Frage b) |
| `probe/*.java` | Quellen der Probe-Programme |
| `serve.py` | **Range-fähiger** Static-Server (CheerpJ braucht HTTP-Range; `python3 -m http.server` reicht NICHT) |
| `build.sh` | erzeugt alle Jars (App Java 17 + 21, Probe, ECJ, rt17/rt8) |

Die Jars selbst sind **nicht eingecheckt** (Build-Artefakte, teils groß) — mit `build.sh` erzeugen.

## Reproduzieren

```bash
cd spike/cheerpj
./build.sh                 # baut/holt alle benötigten Jars
python3 serve.py 8111      # Range-fähiger Server
# dann im Browser öffnen:
#   http://localhost:8111/index.html          (IDE, Java 17)
#   http://localhost:8111/probe.html          (Compiler-/Versions-Probe)
#   http://localhost:8111/ecj-batch-probe.html(ECJ-Compile-Test)
#   http://localhost:8111/app-java21.html     (Java-21 lädt nicht)
```

Probe-Ausgaben erscheinen in der **Browser-Konsole** (DevTools).

## Hinweise

- Getestet mit CheerpJ **4.3** (feste Version im `<script src=…/4.3/loader.js>`).
- CheerpJ-Laufzeit = **OpenJDK 17**; Java-21-Bytecode lädt nicht.
- CheerpJ Community Edition = „personal and non-business use only" → Lizenz für Schule klären.
