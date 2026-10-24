# SimplestJavaIDE – Web (browser-native)

Eine komplett neu, **browser-native** programmierte Version der SimplestJavaIDE:
HTML/CSS/**TypeScript** mit **CodeMirror 6** als Editor und einem **eigenen,
clientseitigen Java-Interpreter** (Lexer → Parser → async Tree-Walk). Kein
Server, kein Java im Browser (keine JVM/WASM) – alles läuft lokal im Browser,
der Code der Schüler:innen bleibt auf dem Gerät.

## Idee

Man schreibt **Methoden ohne Klassen-Drumherum**; `main` ist der Startpunkt.
Der Interpreter deckt den Unterrichts-Umfang ab: Variablen & Datentypen,
Operatoren, `if/else`, Schleifen, Methoden + Rekursion, Arrays, `Scanner`-Eingabe,
`String`/`Math`/`Integer`/`Character`/`ArrayList`/`Random`, `printf`/`String.format`,
Casts – mit **deutschen Fehlermeldungen** inkl. Zeilennummer, **Stop** und
Endlosschleifen-Schutz.

## Entwicklung

```bash
cd webide
npm install
npm run dev      # Dev-Server (http://localhost:5173)
npm run build    # Produktions-Build nach dist/
npm run check    # strenge Typprüfung (tsc)
npm test         # Interpreter-Testsuite (headless)
```

## Hosting (dauerhafter Link)

Reine statische Dateien (`dist/`), deploybar auf jedem Static-Host.

- **GitHub Pages:** per `.github/workflows/pages.yml` automatisch bei Push auf
  `main`/`claude/webide`. Einmalig aktivieren: *Settings → Pages → Source:
  „GitHub Actions"*.
- **GitLab Pages:** per `.gitlab-ci.yml` (braucht einen Docker-Runner). GitLab
  veröffentlicht das `public/`-Artefakt automatisch.

## Aufbau

```
webide/
  index.html
  src/
    main.ts            UI-Steuerung (Toolbar, Editor, Konsole, Shortcuts)
    completion.ts      Autovervollständigung (API, Keywords, Snippets)
    storage.ts         Speichern/Laden (localStorage)
    help.ts            Hilfe-/Über-Dialog
    interpreter/       Lexer, Parser, AST, Werte, async-Interpreter, run()
  test/run-tests.ts    Interpreter-Testsuite
```
